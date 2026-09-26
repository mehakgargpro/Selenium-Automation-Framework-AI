package com.sourav.framework.ai;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record FailureContext(
        String testClass,
        String testMethod,
        String exceptionType,
        String exceptionMessage,
        String stackTrace,
        String currentUrl,
        String browser,
        String environment,
        String screenshotPath,
        String timestamp) {

    private static final int MAX_MESSAGE_LENGTH = 1200;
    private static final int MAX_STACK_TRACE_LENGTH = 6000;
    private static final Pattern SENSITIVE_VALUE = Pattern.compile(
            "(?i)(password|passwd|token|api[\\s_-]*key|authorization|cookie|secret)([\"']?\\s*[:=]\\s*[\"']?)([^\\s,;\"']+)");
    private static final Pattern BEARER_VALUE = Pattern.compile("(?i)\\bBearer\\s+[A-Za-z0-9._~+/-]+=*");

    public FailureContext {
        testClass = redact(testClass);
        testMethod = redact(testMethod);
        exceptionType = redact(exceptionType);
        exceptionMessage = truncate(redact(exceptionMessage), MAX_MESSAGE_LENGTH);
        stackTrace = truncate(redact(stackTrace), MAX_STACK_TRACE_LENGTH);
        currentUrl = redact(stripUrlParameters(currentUrl));
        browser = redact(browser);
        environment = redact(environment);
        screenshotPath = redact(screenshotPath);
        timestamp = redact(timestamp);
    }

    public static FailureContext from(
            ITestResult result,
            WebDriver driver,
            String screenshotPath,
            String browser,
            String environment) {
        Throwable throwable = result.getThrowable();
        StringWriter trace = new StringWriter();
        if (throwable != null) {
            throwable.printStackTrace(new PrintWriter(trace));
        }
        String currentUrl = safeCurrentUrl(driver);
        String testClass = result.getTestClass() == null
                ? "unknown"
                : result.getTestClass().getName();

        return new FailureContext(
                testClass,
                result.getMethod().getMethodName(),
                throwable == null ? "unknown" : throwable.getClass().getName(),
                truncate(redact(throwable == null ? "" : throwable.getMessage()), MAX_MESSAGE_LENGTH),
                truncate(redact(trace.toString()), MAX_STACK_TRACE_LENGTH),
                currentUrl,
                redact(browser),
                redact(environment),
                screenshotPath == null ? "" : screenshotPath,
                Instant.now().toString());
    }

    public static String redact(String value) {
        if (value == null || value.isBlank()) {
            return value == null ? "" : value;
        }
        String withoutBearer = BEARER_VALUE.matcher(value).replaceAll("Bearer [REDACTED]");
        Matcher matcher = SENSITIVE_VALUE.matcher(withoutBearer);
        return matcher.replaceAll("$1$2[REDACTED]");
    }

    private static String safeCurrentUrl(WebDriver driver) {
        if (driver == null) {
            return "";
        }
        try {
            URI uri = URI.create(driver.getCurrentUrl());
            if (uri.getScheme() == null || uri.getHost() == null) {
                return "";
            }
            String port = uri.getPort() < 0 ? "" : ":" + uri.getPort();
            return redact(uri.getScheme() + "://" + uri.getHost() + port
                    + (uri.getRawPath() == null ? "" : uri.getRawPath()));
        } catch (IllegalArgumentException | WebDriverException e) {
            return "";
        }
    }

    private static String stripUrlParameters(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            URI uri = URI.create(value);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return "";
            }
            String port = uri.getPort() < 0 ? "" : ":" + uri.getPort();
            return uri.getScheme() + "://" + uri.getHost() + port
                    + (uri.getRawPath() == null ? "" : uri.getRawPath());
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        return value.substring(0, maxLength) + "\n[truncated]";
    }
}
