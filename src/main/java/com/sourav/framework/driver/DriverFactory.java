package com.sourav.framework.driver;

import com.sourav.framework.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Locale;

public final class DriverFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static WebDriver initDriver() {
        String browser = ConfigManager.getBrowser();
        WebDriver driver = createDriver(browser);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigManager.getInt("pageLoadTimeout")));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(ConfigManager.getInt("scriptTimeout")));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        DRIVER.set(driver);
        LOGGER.info("Initialized {} driver for thread {}", browser, Thread.currentThread().getName());
        return driver;
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver has not been initialized for the current thread.");
        }
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
                LOGGER.info("WebDriver quit for thread {}", Thread.currentThread().getName());
            } finally {
                DRIVER.remove();
            }
        }
    }

    private static WebDriver createDriver(String browser) {
        return switch (browser.toLowerCase(Locale.ROOT)) {
            case "firefox" -> createFirefoxDriver();
            case "edge" -> createEdgeDriver();
            case "chrome", "chromium" -> createChromeDriver();
            default -> createChromeDriver();
        };
    }

    private static WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        configureCommonOptions(options);
        if (ConfigManager.isHeadless()) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (ConfigManager.isHeadless()) {
            options.addArguments("-headless");
        }
        return new FirefoxDriver(options);
    }

    private static WebDriver createEdgeDriver() {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        configureCommonOptions(options);
        if (ConfigManager.isHeadless()) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return new EdgeDriver(options);
    }

    private static void configureCommonOptions(ChromeOptions options) {
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--window-size=1920,1080");
        options.setAcceptInsecureCerts(true);
    }

    private static void configureCommonOptions(EdgeOptions options) {
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--window-size=1920,1080");
        options.setAcceptInsecureCerts(true);
    }
}
