package com.sourav.flipkart.pages;

import com.sourav.framework.config.ConfigManager;
import com.sourav.framework.utilities.ScreenshotUtils;
import com.sourav.framework.waits.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public class BasePage {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasePage.class);
    protected final WebDriver driver;
    protected final WaitUtils waitUtils;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
    }

    protected WebElement find(By locator) {
        return waitUtils.waitForVisible(locator, Duration.ofSeconds(ConfigManager.getExplicitWait()));
    }

    protected List<WebElement> findAll(By locator) {
        return waitUtils.waitForPresence(locator, Duration.ofSeconds(ConfigManager.getExplicitWait()));
    }

    public void open(String url) {
        driver.get(url);
        LOGGER.info("Opened URL: {}", url);
        waitUtils.waitForPageLoad();
    }

    public void click(By locator) {
        WebElement element = waitUtils.waitForClickable(locator, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        try {
            element.click();
        } catch (RuntimeException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    public void clickJs(By locator) {
        WebElement element = find(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    public void type(By locator, String text) {
        WebElement element = waitUtils.waitForVisible(locator, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        element.clear();
        element.sendKeys(text);
    }

    public void clearAndType(By locator, String text) {
        WebElement element = waitUtils.waitForVisible(locator, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        element.clear();
        element.sendKeys(text);
        element.sendKeys(Keys.TAB);
    }

    public String getText(By locator) {
        return find(locator).getText();
    }

    public String getAttribute(By locator, String attributeName) {
        return find(locator).getAttribute(attributeName);
    }

    public boolean isDisplayed(By locator) {
        try {
            return find(locator).isDisplayed();
        } catch (NoSuchElementException ignored) {
            return false;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public boolean isEnabled(By locator) {
        try {
            return find(locator).isEnabled();
        } catch (NoSuchElementException ignored) {
            return false;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public void hover(By locator) {
        WebElement element = find(locator);
        new Actions(driver).moveToElement(element).perform();
    }

    public void scrollToElement(By locator) {
        WebElement element = find(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    public void selectDropdownOption(By locator, String visibleText) {
        WebElement element = find(locator);
        new Select(element).selectByVisibleText(visibleText);
    }

    public String captureScreenshot(String testName) {
        return ScreenshotUtils.capture(driver, testName);
    }

    public void waitForPageLoad() {
        waitUtils.waitForPageLoad();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public boolean waitForUrl(String urlPart) {
        return waitUtils.waitForUrl(urlPart);
    }

    public boolean waitForText(By locator, String text) {
        return waitUtils.waitForText(locator, text);
    }
}
