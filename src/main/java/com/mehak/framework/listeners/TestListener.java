package com.mehak.framework.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.mehak.framework.ai.AiFailureAnalysis;
import com.mehak.framework.ai.AiFailureAnalyzer;
import com.mehak.framework.ai.FailureContext;
import com.mehak.framework.config.ConfigManager;
import com.mehak.framework.driver.DriverFactory;
import com.mehak.framework.reporting.ExtentReportManager;
import com.mehak.framework.utilities.ScreenshotUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.util.function.Supplier;

public class TestListener implements ITestListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestListener.class);
    private final Supplier<AiFailureAnalyzer> analyzerFactory;

    public TestListener() {
        this(AiFailureAnalyzer::configured);
    }

    TestListener(Supplier<AiFailureAnalyzer> analyzerFactory) {
        this.analyzerFactory = analyzerFactory;
    }

    @Override
    public void onTestStart(ITestResult result) {
        ConfigManager.load();
        String browser = ConfigManager.getBrowser();
        String environment = ConfigManager.get("environment", "qa");
        ExtentTest test = ExtentReportManager.startTest(result.getMethod().getMethodName(), browser, environment);
        LOGGER.info("Test started: {} [{}]", result.getMethod().getMethodName(), environment);
        if (test != null) {
            test.info("Browser: " + browser + ", Environment: " + environment);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOGGER.info("Test passed: {}", result.getMethod().getMethodName());
        if (ExtentReportManager.getCurrentTest() != null) {
            ExtentReportManager.getCurrentTest().pass("Test passed");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();
        String exceptionType = throwable == null ? "unknown" : throwable.getClass().getName();
        String exceptionMessage = FailureContext.redact(throwable == null ? "" : throwable.getMessage());
        LOGGER.error("Test failed: {} - {}: {}", methodName, exceptionType, exceptionMessage);
        if (ExtentReportManager.getCurrentTest() != null) {
            ExtentReportManager.getCurrentTest().fail(exceptionType + ": " + exceptionMessage);
        }
        WebDriver driver = null;
        String screenshotPath = "";
        try {
            driver = DriverFactory.getDriver();
            screenshotPath = ScreenshotUtils.capture(driver, methodName);
            ExtentReportManager.attachScreenshot(screenshotPath, methodName);
        } catch (IllegalStateException | WebDriverException e) {
            LOGGER.warn("Unable to capture failure screenshot for {}", methodName);
        }
        analyzeFailure(result, driver, screenshotPath);
    }

    void analyzeFailure(ITestResult result, WebDriver driver, String screenshotPath) {
        if (!AiFailureAnalyzer.isEnabled()) {
            LOGGER.info("AI failure analysis is disabled");
            return;
        }

        FailureContext context = FailureContext.from(
                result,
                driver,
                screenshotPath,
                ConfigManager.getBrowser(),
                ConfigManager.get("environment", "qa"));
        analyzeFailure(context);
    }

    void analyzeFailure(FailureContext context) {
        LOGGER.info("AI failure analysis started for {}.{}", context.testClass(), context.testMethod());
        try {
            AiFailureAnalysis analysis = analyzerFactory.get().analyze(context);
            LOGGER.info("AI failure analysis completed: {}", analysis.category());
            ExtentTest test = ExtentReportManager.getCurrentTest();
            if (test != null) {
                test.info("AI Failure Analysis");
                test.info("Category: " + analysis.category());
                test.info("Probable Cause: " + analysis.probableCause());
                test.info("Confidence: " + analysis.confidence());
                test.info("Recommended Action: " + analysis.recommendedAction());
                test.info("Explanation: " + analysis.explanation());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("AI failure analysis unavailable; continuing test reporting");
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("AI failure analysis unavailable; continuing test reporting");
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOGGER.warn("Test skipped: {}", result.getMethod().getMethodName());
        if (ExtentReportManager.getCurrentTest() != null) {
            ExtentReportManager.getCurrentTest().skip(result.getThrowable());
        }
    }

    @Override
    public void onStart(ITestContext context) {
        ExtentReportManager.init();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flush();
    }
}
