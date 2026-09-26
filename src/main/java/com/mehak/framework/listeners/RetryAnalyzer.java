package com.mehak.framework.listeners;

import com.mehak.framework.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryAnalyzer.class);
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = ConfigManager.getInt("test.retryCount");
        if (retryCount < maxRetries) {
            retryCount++;
            LOGGER.warn("Retrying test [{}] attempt {} of {}", result.getName(), retryCount, maxRetries + 1);
            return true;
        }
        return false;
    }
}
