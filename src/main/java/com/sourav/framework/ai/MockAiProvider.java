package com.sourav.framework.ai;

import java.io.IOException;

public final class MockAiProvider implements AiProvider {
    @Override
    public AiFailureAnalysis analyze(AiAnalysisRequest request) throws IOException {
        return new AiFailureAnalysis(
                AiFailureAnalysis.FailureCategory.ELEMENT_NOT_FOUND,
                "The expected element may not be present in the current page state.",
                AiFailureAnalysis.Confidence.HIGH,
                "Check the locator against the current page DOM and screenshot.",
                "Deterministic mock analysis; no external AI service was called.");
    }
}
