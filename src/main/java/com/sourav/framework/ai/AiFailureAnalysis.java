package com.sourav.framework.ai;

import java.util.Objects;

public record AiFailureAnalysis(
        FailureCategory category,
        String probableCause,
        Confidence confidence,
        String recommendedAction,
        String explanation) {

    public AiFailureAnalysis {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(confidence, "confidence");
        probableCause = requireText(probableCause, "probableCause");
        recommendedAction = requireText(recommendedAction, "recommendedAction");
        explanation = requireText(explanation, "explanation");
    }

    public static AiFailureAnalysis evidenceFallback(FailureContext context) {
        FailureCategory category = FailureCategory.fromExceptionType(context.exceptionType());
        return new AiFailureAnalysis(
                category,
                "The AI response could not be validated; only the captured exception evidence is available.",
                Confidence.LOW,
                "Review the exception and captured screenshot manually.",
                "This fallback uses the exception type and message only; the underlying cause is uncertain.");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public enum Confidence {
        HIGH,
        MEDIUM,
        LOW;

        public static Confidence fromProbability(double probability) {
            if (!Double.isFinite(probability) || probability < 0.0 || probability > 1.0) {
                throw new IllegalArgumentException("Confidence probability must be between 0 and 1");
            }
            if (probability >= 0.75) {
                return HIGH;
            }
            if (probability >= 0.5) {
                return MEDIUM;
            }
            return LOW;
        }
    }

    public enum FailureCategory {
        LOCATOR_ISSUE,
        ELEMENT_NOT_FOUND,
        TIMEOUT,
        STALE_ELEMENT,
        ASSERTION_FAILURE,
        NETWORK_ISSUE,
        APPLICATION_ERROR,
        CONFIGURATION_ERROR,
        ENVIRONMENT_ISSUE,
        UNKNOWN;

        public static FailureCategory fromExceptionType(String exceptionType) {
            if (exceptionType == null) {
                return UNKNOWN;
            }
            if (exceptionType.endsWith("NoSuchElementException")) {
                return ELEMENT_NOT_FOUND;
            }
            if (exceptionType.endsWith("TimeoutException")) {
                return TIMEOUT;
            }
            if (exceptionType.endsWith("StaleElementReferenceException")) {
                return STALE_ELEMENT;
            }
            if (exceptionType.endsWith("AssertionError")) {
                return ASSERTION_FAILURE;
            }
            if (exceptionType.endsWith("ConnectException") || exceptionType.endsWith("UnknownHostException")) {
                return NETWORK_ISSUE;
            }
            return UNKNOWN;
        }
    }
}
