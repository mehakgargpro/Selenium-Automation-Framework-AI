package com.mehak.framework.ai;

import com.sun.net.httpserver.HttpServer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public class JevProviderTest {
    private static final String VALID_RESPONSE = """
            {
              "model": "jev-1.13.0",
              "answers": {
                "category": {"type": "choice", "choice": "ELEMENT_NOT_FOUND", "confidence": 0.92},
                "probable_cause": {"type": "choice", "choice": "element_absent", "confidence": 0.86},
                "recommended_action": {"type": "choice", "choice": "inspect_locator_and_dom", "confidence": 0.9},
                "evidence_strength": {"type": "score", "score": 2.0, "confidence": 0.9}
              },
              "usage": {"input_tokens": 10, "output_tokens": 5}
            }
            """;

    @Test
    public void shouldParseDocumentedJevResponseAndSendDocumentedRequest() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        HttpServer server = startServer(200, VALID_RESPONSE, 0, requestBody, authorization);
        try {
            JevProvider provider = provider(server, "test-key", Duration.ofSeconds(3));
            AiFailureAnalysis result = provider.analyze(AiAnalysisRequest.from(context()));

            Assert.assertEquals(result.category(), AiFailureAnalysis.FailureCategory.ELEMENT_NOT_FOUND);
            Assert.assertEquals(result.confidence(), AiFailureAnalysis.Confidence.HIGH);
            Assert.assertTrue(result.recommendedAction().contains("locator"));
            Assert.assertEquals(authorization.get(), "test-key");
            Assert.assertTrue(requestBody.get().contains("\"model\":\"jev-latest\""));
            Assert.assertTrue(requestBody.get().contains("\"type\":\"choice\""));
            Assert.assertTrue(requestBody.get().contains("\"type\":\"score\""));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void shouldRejectMalformedJevResponseForAnalyzerFallback() throws Exception {
        HttpServer server = startServer(200, "not-json", 0, new AtomicReference<>(), new AtomicReference<>());
        try {
            JevProvider provider = provider(server, "test-key", Duration.ofSeconds(3));
            AiFailureAnalysis result = new AiFailureAnalyzer(provider).analyze(context());
            Assert.assertEquals(result.category(), AiFailureAnalysis.FailureCategory.ELEMENT_NOT_FOUND);
            Assert.assertEquals(result.confidence(), AiFailureAnalysis.Confidence.LOW);
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void shouldFailFastWhenApiKeyIsMissing() throws Exception {
        JevProvider provider = new JevProvider(
                HttpClient.newHttpClient(),
                URI.create("http://127.0.0.1:1/v1/systemone"),
                "",
                Duration.ofSeconds(1),
                new com.fasterxml.jackson.databind.ObjectMapper());

        Assert.assertFalse(provider.isConfigured());
        Assert.expectThrows(IllegalStateException.class,
                () -> provider.analyze(AiAnalysisRequest.from(context())));
    }

    @Test
    public void shouldPropagateProviderTimeoutAsAnIoFailure() throws Exception {
        HttpServer server = startServer(
                200, VALID_RESPONSE, 700, new AtomicReference<>(), new AtomicReference<>());
        try {
            JevProvider provider = provider(server, "test-key", Duration.ofMillis(100));
            Assert.expectThrows(IOException.class,
                    () -> provider.analyze(AiAnalysisRequest.from(context())));
        } finally {
            server.stop(0);
        }
    }

    private static JevProvider provider(HttpServer server, String apiKey, Duration timeout) {
        URI endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/v1/systemone");
        return new JevProvider(
                HttpClient.newBuilder().connectTimeout(timeout).build(),
                endpoint,
                apiKey,
                timeout,
                new com.fasterxml.jackson.databind.ObjectMapper());
    }

    private static HttpServer startServer(
            int status,
            String responseBody,
            long delayMillis,
            AtomicReference<String> requestBody,
            AtomicReference<String> authorization) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/systemone", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            if (delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Test server interrupted", e);
                }
            }
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            try {
                exchange.sendResponseHeaders(status, bytes.length);
                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(bytes);
                }
            } catch (IOException ignored) {
                // A timed-out test client may close the connection before the delayed response.
            } finally {
                exchange.close();
            }
        });
        server.start();
        return server;
    }

    private static FailureContext context() {
        return new FailureContext(
                "com.example.SearchTest",
                "shouldFindProduct",
                "org.openqa.selenium.NoSuchElementException",
                "Product element was not found",
                "sample stack trace",
                "https://www.flipkart.com/search?q=laptop",
                "chrome",
                "qa",
                "C:\\screenshots\\failure.png",
                Instant.now().toString());
    }
}
