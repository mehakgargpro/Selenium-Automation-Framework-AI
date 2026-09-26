package com.sourav.framework.listeners;

import com.sourav.framework.ai.AiFailureAnalyzer;
import com.sourav.framework.reporting.ExtentReportManager;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

public class TestListenerAiTest {

    @Test
    public void shouldContinueFailureReportingWhenAiProviderTimesOut() {
        String previous = System.getProperty("ai.enabled");
        try {
            System.setProperty("ai.enabled", "true");
            Supplier<AiFailureAnalyzer> failingAnalyzer = () -> new AiFailureAnalyzer(request -> {
                throw new IOException("simulated provider timeout");
            });
            TestListener listener = new TestListener(failingAnalyzer);
            var currentReportTest = ExtentReportManager.getCurrentTest();
            ExtentReportManager.setCurrentTest(null);
            try {
                listener.onTestFailure(failedTestResult());
            } finally {
                ExtentReportManager.setCurrentTest(currentReportTest);
            }
        } finally {
            if (previous == null) {
                System.clearProperty("ai.enabled");
            } else {
                System.setProperty("ai.enabled", previous);
            }
        }
    }

    private static ITestResult failedTestResult() {
        ITestNGMethod method = (ITestNGMethod) Proxy.newProxyInstance(
                ITestNGMethod.class.getClassLoader(),
                new Class<?>[] { ITestNGMethod.class },
                (proxy, invoked, args) -> "getMethodName".equals(invoked.getName()) ? "sampleFailure" : null);
        ITestClass testClass = (ITestClass) Proxy.newProxyInstance(
                ITestClass.class.getClassLoader(),
                new Class<?>[] { ITestClass.class },
                (proxy, invoked, args) -> "getName".equals(invoked.getName()) ? "com.example.SampleTest" : null);

        return (ITestResult) Proxy.newProxyInstance(
                ITestResult.class.getClassLoader(),
                new Class<?>[] { ITestResult.class },
                (proxy, invoked, args) -> switch (invoked.getName()) {
                    case "getMethod" -> method;
                    case "getTestClass" -> testClass;
                    case "getThrowable" -> new AssertionError("synthetic test failure");
                    default -> null;
                });
    }
}
