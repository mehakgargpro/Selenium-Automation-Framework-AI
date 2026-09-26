package com.sourav.framework.ai;

import java.io.IOException;

public interface AiProvider {
    AiFailureAnalysis analyze(AiAnalysisRequest request) throws IOException, InterruptedException;
}
