package com.sourav.framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ExtentReportManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtentReportManager.class);
    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();
    private static ExtentReports extentReports;

    private ExtentReportManager() {
    }

    public static void init() {
        if (extentReports != null) {
            return;
        }
        Path reportDir = Paths.get("test-output", "reports");
        reportDir.toFile().mkdirs();
        File reportFile = reportDir.resolve("extent-report.html").toFile();
        ExtentSparkReporter reporter = new ExtentSparkReporter(reportFile);
        reporter.config().setDocumentTitle("Flipkart Automation Report");
        reporter.config().setReportName("Selenium Framework Execution");
        extentReports = new ExtentReports();
        extentReports.attachReporter(reporter);
        LOGGER.info("Extent report initialized at {}", reportFile.getAbsolutePath());
    }

    public static void flush() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }

    public static ExtentTest startTest(String name, String browser, String environment) {
        init();
        ExtentTest test = extentReports.createTest(name)
                .assignCategory(environment)
                .assignDevice(browser);
        TEST.set(test);
        return test;
    }

    public static ExtentTest getCurrentTest() {
        return TEST.get();
    }

    public static void setCurrentTest(ExtentTest test) {
        TEST.set(test);
    }

    public static void attachScreenshot(String filePath, String testName) {
        ExtentTest test = TEST.get();
        if (test == null || filePath == null || filePath.isBlank()) {
            return;
        }
        try {
            test.info("Screenshot captured for " + testName, MediaEntityBuilder.createScreenCaptureFromPath(filePath).build());
        } catch (Exception e) {
            LOGGER.warn("Failed to attach screenshot {} to report", filePath, e);
        }
    }
}
