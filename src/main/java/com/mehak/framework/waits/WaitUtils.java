package com.mehak.framework.waits;

import com.mehak.framework.config.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.List;

public class WaitUtils {
    private final WebDriver driver;
    private final Duration timeout;

    public WaitUtils(WebDriver driver) {
        this.driver = driver;
        this.timeout = Duration.ofSeconds(ConfigManager.getExplicitWait());
    }

    public WebElement waitForVisible(By locator) {
        return waitForVisible(locator, timeout);
    }

    public WebElement waitForVisible(By locator, Duration timeout) {
        return new FluentWait<>(driver)
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(Exception.class)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForClickable(By locator) {
        return waitForClickable(locator, timeout);
    }

    public WebElement waitForClickable(By locator, Duration timeout) {
        return new FluentWait<>(driver)
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(Exception.class)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    public List<WebElement> waitForPresence(By locator) {
        return waitForPresence(locator, timeout);
    }

    public List<WebElement> waitForPresence(By locator, Duration timeout) {
        return new FluentWait<>(driver)
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(Exception.class)
                .until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    public boolean waitForInvisible(By locator) {
        return waitForInvisible(locator, timeout);
    }

    public boolean waitForInvisible(By locator, Duration timeout) {
        return new FluentWait<>(driver)
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(Exception.class)
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public boolean waitForUrl(String urlPart) {
        return waitForUrl(urlPart, timeout);
    }

    public boolean waitForUrl(String urlPart, Duration timeout) {
        return new FluentWait<>(driver)
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(Exception.class)
                .until(ExpectedConditions.urlContains(urlPart));
    }

    public boolean waitForTitle(String titleFragment) {
        return waitForTitle(titleFragment, timeout);
    }

    public boolean waitForTitle(String titleFragment, Duration timeout) {
        return new FluentWait<>(driver)
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(Exception.class)
                .until(ExpectedConditions.titleContains(titleFragment));
    }

    public boolean waitForText(By locator, String text) {
        return waitForText(locator, text, timeout);
    }

    public boolean waitForText(By locator, String text, Duration timeout) {
        return new FluentWait<>(driver)
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(Exception.class)
                .until(webDriver -> {
                    List<WebElement> elements = webDriver.findElements(locator);
                    return elements.stream().anyMatch(el -> el.getText() != null && el.getText().contains(text));
                });
    }

    public boolean waitForElementToDisappear(By locator) {
        return waitForElementToDisappear(locator, timeout);
    }

    public boolean waitForElementToDisappear(By locator, Duration timeout) {
        return new FluentWait<>(driver)
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(Exception.class)
                .until(webDriver -> {
                    List<WebElement> elements = webDriver.findElements(locator);
                    return elements.isEmpty() || elements.stream().allMatch(el -> !el.isDisplayed());
                });
    }

    public void waitForPageLoad() {
        new FluentWait<>(driver)
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(Exception.class)
                .until((ExpectedCondition<Boolean>) webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }
}
