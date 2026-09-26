package com.sourav.flipkart.tests;

import com.sourav.framework.config.ConfigManager;
import com.sourav.framework.driver.DriverFactory;
import com.sourav.framework.reporting.ExtentReportManager;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.lang.reflect.Method;

public class BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);
    protected WebDriver driver;

    @BeforeSuite
    public void beforeSuite() {
        ConfigManager.load();
        ExtentReportManager.init();
    }

    @BeforeMethod
    public void setUp(Method method) {
        driver = DriverFactory.initDriver();
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        LOGGER.info("Starting test method: {}", method.getName());
    }

    @AfterMethod
    public void tearDown() {
        try {
            DriverFactory.quitDriver();
        } finally {
            driver = null;
        }
    }

    @AfterSuite
    public void afterSuite() {
        ExtentReportManager.flush();
    }

    protected void failIfAutomationBlocked(String message) {
        Assert.fail(message);
    }
}
