package com.mehak.framework.ai;

import java.io.IOException;

public interface JevClient extends AiProvider {
    boolean isConfigured();

    @Override
    AiFailureAnalysis analyze(AiAnalysisRequest request) throws IOException, InterruptedException;
}
