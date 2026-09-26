package com.sourav.framework.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class JevProvider implements JevClient {
    static final URI API_ENDPOINT = URI.create("https://api.typesafe.ai/v1/systemone");

    private final HttpClient httpClient;
    private final URI endpoint;
    private final String apiKey;
    private final Duration requestTimeout;
    private final ObjectMapper objectMapper;

    public JevProvider(String apiKey, Duration requestTimeout) {
        this(HttpClient.newBuilder()
                        .connectTimeout(requestTimeout)
                        .build(),
                API_ENDPOINT,
                apiKey,
                requestTimeout,
                new ObjectMapper());
    }

    JevProvider(HttpClient httpClient, URI endpoint, String apiKey, Duration requestTimeout, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.endpoint = endpoint;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.requestTimeout = requestTimeout;
        this.objectMapper = objectMapper;
    }

    public static JevProvider fromEnvironment(Duration requestTimeout) {
        return new JevProvider(System.getenv("TYPESAFE_API_KEY"), requestTimeout);
    }

    @Override
    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    @Override
    public AiFailureAnalysis analyze(AiAnalysisRequest request) throws IOException, InterruptedException {
        if (!isConfigured()) {
            throw new IllegalStateException("TYPESAFE_API_KEY is not configured");
        }

        String requestBody = objectMapper.writeValueAsString(request.toJevPayload());
        HttpRequest httpRequest = HttpRequest.newBuilder(endpoint)
                .timeout(requestTimeout)
                .header("Authorization", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("TypeSafe API returned HTTP status " + response.statusCode());
        }
        return parseAnalysis(response.body(), request);
    }

    private AiFailureAnalysis parseAnalysis(String responseBody, AiAnalysisRequest request)
            throws AiResponseParsingException {
        try {
            JsonNode answers = objectMapper.readTree(responseBody).path("answers");
            String category = choiceValue(answers, "category");
            String cause = choiceValue(answers, "probable_cause");
            String action = choiceValue(answers, "recommended_action");
            double confidence = choiceConfidence(answers, "category");
            confidence = Math.min(confidence, choiceConfidence(answers, "probable_cause"));
            confidence = Math.min(confidence, choiceConfidence(answers, "recommended_action"));
            double evidenceStrength = scoreValue(answers, "evidence_strength");

            AiFailureAnalysis.FailureCategory failureCategory =
                    AiFailureAnalysis.FailureCategory.valueOf(category);
            String causeText = causeDescription(cause);
            String actionText = actionDescription(action);
            AiFailureAnalysis.Confidence confidenceLevel =
                    AiFailureAnalysis.Confidence.fromProbability(Math.min(confidence, evidenceStrength / 2.0));
            if (failureCategory == AiFailureAnalysis.FailureCategory.UNKNOWN
                    || "insufficient_evidence".equals(cause)) {
                confidenceLevel = AiFailureAnalysis.Confidence.LOW;
            }
            String explanation = "Jev selected " + failureCategory
                    + " because the supplied failure evidence best matches: " + causeText
                    + exceptionEvidence(request);
            return new AiFailureAnalysis(failureCategory, causeText, confidenceLevel, actionText, explanation);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new AiResponseParsingException("TypeSafe response did not contain a valid analysis", e);
        }
    }

    private static String exceptionEvidence(AiAnalysisRequest request) {
        Object message = request.state().get("exception_message");
        if (!(message instanceof String text) || text.isBlank()) {
            return ".";
        }
        String concise = text.length() <= 240 ? text : text.substring(0, 240) + "...";
        return ". Captured exception message: " + concise;
    }

    private static String choiceValue(JsonNode answers, String question) throws AiResponseParsingException {
        JsonNode answer = answers.path(question);
        if (!"choice".equals(answer.path("type").asText())) {
            throw new AiResponseParsingException("TypeSafe response is missing choice answer: " + question);
        }
        String value = answer.path("choice").asText();
        if (value.isBlank()) {
            throw new AiResponseParsingException("TypeSafe response has an empty choice answer: " + question);
        }
        return value;
    }

    private static double choiceConfidence(JsonNode answers, String question) throws AiResponseParsingException {
        double confidence = answers.path(question).path("confidence").asDouble(Double.NaN);
        if (!Double.isFinite(confidence) || confidence < 0.0 || confidence > 1.0) {
            throw new AiResponseParsingException("TypeSafe response has invalid confidence");
        }
        return confidence;
    }

    private static double scoreValue(JsonNode answers, String question) throws AiResponseParsingException {
        JsonNode answer = answers.path(question);
        if (!"score".equals(answer.path("type").asText())) {
            throw new AiResponseParsingException("TypeSafe response is missing score answer: " + question);
        }
        double score = answer.path("score").asDouble(Double.NaN);
        if (!Double.isFinite(score) || score < 0.0 || score > 2.0) {
            throw new AiResponseParsingException("TypeSafe response has invalid evidence strength");
        }
        return score;
    }

    private static String causeDescription(String value) throws AiResponseParsingException {
        return switch (value) {
            case "wrong_or_changed_locator" -> "the locator may be incorrect or no longer match the current DOM";
            case "element_absent" -> "the target element was not present in the loaded page state";
            case "wait_expired" -> "the expected wait condition did not become true before timeout";
            case "dom_replaced" -> "the element may have been detached or replaced by a DOM update";
            case "assertion_mismatch" -> "the observed value did not satisfy the test assertion";
            case "network_or_navigation" -> "a request, DNS lookup, or page navigation may have failed";
            case "application_response" -> "the page or application may have returned an error state";
            case "invalid_configuration" -> "a required setting or runtime configuration may be invalid";
            case "environment_unavailable" -> "a browser, driver, or external environment dependency may be unavailable";
            case "insufficient_evidence" -> "the supplied evidence does not establish a probable cause";
            default -> throw new AiResponseParsingException("Unknown cause choice");
        };
    }

    private static String actionDescription(String value) throws AiResponseParsingException {
        return switch (value) {
            case "inspect_locator_and_dom" -> "Check the locator against the current page DOM and screenshot.";
            case "inspect_page_state" -> "Inspect the current page, URL, and screenshot for overlays or alternate content.";
            case "review_wait_condition" -> "Check the wait target, timeout, and whether the page finished loading.";
            case "check_dom_refresh" -> "Reacquire the element after page updates and inspect the failing interaction.";
            case "compare_assertion_values" -> "Compare the expected and actual values in the assertion.";
            case "check_network_and_navigation" -> "Check navigation timing, DNS, connectivity, and response availability.";
            case "inspect_application_error" -> "Inspect the visible application error and relevant server response.";
            case "review_runtime_configuration" -> "Verify the browser, driver, environment, and required settings.";
            case "rerun_with_diagnostics" -> "Re-run once with browser logs and the captured page state enabled.";
            case "manual_review" -> "Review the failure evidence manually before changing the test.";
            default -> throw new AiResponseParsingException("Unknown recommended action choice");
        };
    }
}
