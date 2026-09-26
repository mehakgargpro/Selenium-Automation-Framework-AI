package com.mehak.framework.ai;

import java.util.LinkedHashMap;
import java.util.Map;

public record AiAnalysisRequest(Map<String, Object> state, Map<String, Object> questions) {
    public static final String MODEL = "jev-latest";

    public AiAnalysisRequest {
        state = Map.copyOf(state);
        questions = Map.copyOf(questions);
    }

    public static AiAnalysisRequest from(FailureContext context) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("test_class", context.testClass());
        state.put("test_method", context.testMethod());
        state.put("exception_type", context.exceptionType());
        state.put("exception_message", context.exceptionMessage());
        state.put("stack_trace", context.stackTrace());
        state.put("current_url", context.currentUrl());
        state.put("browser", context.browser());
        state.put("environment", context.environment());
        state.put("timestamp", context.timestamp());

        Map<String, Object> questions = new LinkedHashMap<>();
        questions.put("category", choice(
                "Classify the automated test failure using only supplied evidence. "
                        + "Do not invent application behavior. Choose UNKNOWN if evidence is insufficient.",
                categoryCriteria()));
        questions.put("probable_cause", choice(
                "Choose the most probable cause supported by supplied evidence. "
                        + "Do not claim certainty; choose insufficient_evidence when unsure.",
                causeCriteria()));
        questions.put("recommended_action", choice(
                "Choose the single most useful next debugging action based only on supplied evidence.",
                actionCriteria()));
        questions.put("evidence_strength", Map.of(
                "type", "score",
                "instructions", "Rate how strongly the supplied evidence supports the chosen category and cause.",
                "criteria", new String[] {
                        "Insufficient or ambiguous evidence",
                        "Some evidence, but multiple causes remain plausible",
                        "Strong direct evidence for the selected diagnosis"
                }));

        return new AiAnalysisRequest(state, questions);
    }

    public Map<String, Object> toJevPayload() {
        return Map.of(
                "state", state,
                "model", MODEL,
                "questions", questions);
    }

    private static Map<String, Object> choice(String instructions, Map<String, String> criteria) {
        return Map.of("type", "choice", "instructions", instructions, "criteria", criteria);
    }

    private static Map<String, String> categoryCriteria() {
        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("LOCATOR_ISSUE", "A selector or locator is incorrect or no longer matches the intended control.");
        criteria.put("ELEMENT_NOT_FOUND", "The expected element is absent from the currently loaded page.");
        criteria.put("TIMEOUT", "An explicit or page-load wait expired before its condition became true.");
        criteria.put("STALE_ELEMENT", "The DOM replaced an element reference before the interaction completed.");
        criteria.put("ASSERTION_FAILURE", "An assertion compared an actual result with an unmet expected result.");
        criteria.put("NETWORK_ISSUE", "Evidence indicates a network, DNS, or navigation transport failure.");
        criteria.put("APPLICATION_ERROR", "Evidence indicates an application-side error page or response.");
        criteria.put("CONFIGURATION_ERROR", "A test or framework setting is missing or invalid.");
        criteria.put("ENVIRONMENT_ISSUE", "A browser, driver, host, or external environment dependency failed.");
        criteria.put("UNKNOWN", "Evidence is insufficient or does not support another category.");
        return criteria;
    }

    private static Map<String, String> causeCriteria() {
        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("wrong_or_changed_locator", "The locator may be incorrect or no longer match the current DOM.");
        criteria.put("element_absent", "The target element was not present in the loaded page state.");
        criteria.put("wait_expired", "The expected wait condition did not become true before timeout.");
        criteria.put("dom_replaced", "The element may have been detached or replaced by a DOM update.");
        criteria.put("assertion_mismatch", "The observed value did not satisfy the test assertion.");
        criteria.put("network_or_navigation", "A request, DNS lookup, or page navigation may have failed.");
        criteria.put("application_response", "The page or application may have returned an error state.");
        criteria.put("invalid_configuration", "A required setting or runtime configuration may be invalid.");
        criteria.put("environment_unavailable", "A browser, driver, or external environment dependency may be unavailable.");
        criteria.put("insufficient_evidence", "The supplied exception and context do not establish a probable cause.");
        return criteria;
    }

    private static Map<String, String> actionCriteria() {
        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("inspect_locator_and_dom", "Check the locator against the current page DOM and screenshot.");
        criteria.put("inspect_page_state", "Inspect the current page, URL, and screenshot for overlays or alternate content.");
        criteria.put("review_wait_condition", "Check the wait target, timeout, and whether the page finished loading.");
        criteria.put("check_dom_refresh", "Reacquire the element after page updates and inspect the failing interaction.");
        criteria.put("compare_assertion_values", "Compare the expected and actual values in the assertion.");
        criteria.put("check_network_and_navigation", "Check navigation timing, DNS, connectivity, and response availability.");
        criteria.put("inspect_application_error", "Inspect the visible application error and relevant server response.");
        criteria.put("review_runtime_configuration", "Verify the browser, driver, environment, and required settings.");
        criteria.put("rerun_with_diagnostics", "Re-run once with browser logs and the captured page state enabled.");
        criteria.put("manual_review", "Review the failure evidence manually before changing the test.");
        return criteria;
    }
}
