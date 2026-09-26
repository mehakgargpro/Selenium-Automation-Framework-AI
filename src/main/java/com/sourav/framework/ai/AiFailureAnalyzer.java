package com.sourav.framework.ai;

import com.sourav.framework.config.ConfigManager;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;

public final class AiFailureAnalyzer {
    private final AiProvider provider;

    public AiFailureAnalyzer(AiProvider provider) {
        this.provider = provider;
    }

    public static boolean isEnabled() {
        return ConfigManager.getBoolean("ai.enabled");
    }

    public static AiFailureAnalyzer configured() {
        String providerName = ConfigManager.get("ai.provider", "jev").toLowerCase(Locale.ROOT);
        Duration timeout = Duration.ofSeconds(ConfigManager.getInt("ai.timeout.seconds"));
        AiProvider provider = switch (providerName) {
            case "jev" -> JevProvider.fromEnvironment(timeout);
            case "mock" -> new MockAiProvider();
            default -> throw new IllegalArgumentException("Unsupported AI provider: " + providerName);
        };
        return new AiFailureAnalyzer(provider);
    }

    public AiFailureAnalysis analyze(FailureContext context) throws IOException, InterruptedException {
        try {
            return provider.analyze(AiAnalysisRequest.from(context));
        } catch (AiResponseParsingException e) {
            return AiFailureAnalysis.evidenceFallback(context);
        }
    }

    public static String sanitizeForPayload(String value) {
        return FailureContext.redact(value);
    }
}
