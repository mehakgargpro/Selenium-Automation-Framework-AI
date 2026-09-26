package com.mehak.framework.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.Instant;

public class AiFailureAnalyzerTest {

    @Test
    public void shouldRespectDisabledConfiguration() {
        String previous = System.getProperty("ai.enabled");
        try {
            System.setProperty("ai.enabled", "false");
            Assert.assertFalse(AiFailureAnalyzer.isEnabled());
        } finally {
            restoreProperty("ai.enabled", previous);
        }
    }

    @Test
    public void shouldReturnDeterministicMockAnalysis() throws Exception {
        AiFailureAnalysis analysis = new AiFailureAnalyzer(new MockAiProvider()).analyze(context());

        Assert.assertEquals(analysis.category(), AiFailureAnalysis.FailureCategory.ELEMENT_NOT_FOUND);
        Assert.assertEquals(analysis.confidence(), AiFailureAnalysis.Confidence.HIGH);
        Assert.assertTrue(analysis.explanation().contains("Deterministic mock"));
    }

    @Test
    public void shouldSelectMockProviderFromConfiguration() throws Exception {
        String previous = System.getProperty("ai.provider");
        try {
            System.setProperty("ai.provider", "mock");
            AiFailureAnalysis analysis = AiFailureAnalyzer.configured().analyze(context());
            Assert.assertEquals(analysis.category(), AiFailureAnalysis.FailureCategory.ELEMENT_NOT_FOUND);
        } finally {
            restoreProperty("ai.provider", previous);
        }
    }

    @Test
    public void shouldFallbackToEvidenceOnlyTextForMalformedProviderResponse() throws Exception {
        AiProvider malformedProvider = request -> {
            throw new AiResponseParsingException("Malformed response");
        };
        AiFailureAnalysis analysis = new AiFailureAnalyzer(malformedProvider).analyze(context());

        Assert.assertEquals(analysis.category(), AiFailureAnalysis.FailureCategory.ELEMENT_NOT_FOUND);
        Assert.assertEquals(analysis.confidence(), AiFailureAnalysis.Confidence.LOW);
        Assert.assertTrue(analysis.explanation().contains("cause is uncertain"));
    }

    @Test
    public void shouldRedactSensitiveInformationAndOmitScreenshotPath() throws IOException {
        FailureContext context = new FailureContext(
                "com.example.LoginTest",
                "testLogin",
                "org.openqa.selenium.WebDriverException",
                "password=supersecret token=abc123 Authorization=Bearer privatevalue",
                "Cookie: session=secret-cookie",
                "https://example.test/login?token=query-secret#fragment",
                "chrome",
                "qa",
                "C:\\private\\screenshots\\failure.png",
                Instant.now().toString());

        String payload = new ObjectMapper().writeValueAsString(AiAnalysisRequest.from(context).toJevPayload());

        Assert.assertFalse(payload.contains("supersecret"));
        Assert.assertFalse(payload.contains("abc123"));
        Assert.assertFalse(payload.contains("privatevalue"));
        Assert.assertFalse(payload.contains("secret-cookie"));
        Assert.assertFalse(payload.contains("query-secret"));
        Assert.assertFalse(payload.contains("failure.png"));
        Assert.assertTrue(payload.contains("[REDACTED]"));
    }

    private static FailureContext context() {
        return new FailureContext(
                "com.example.SearchTest",
                "shouldFindProduct",
                "org.openqa.selenium.NoSuchElementException",
                "Element was not found",
                "sample stack trace",
                "https://www.flipkart.com/search",
                "chrome",
                "qa",
                "",
                Instant.now().toString());
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
