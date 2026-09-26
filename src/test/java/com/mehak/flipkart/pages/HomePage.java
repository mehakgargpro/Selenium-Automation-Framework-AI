package com.mehak.flipkart.pages;

import com.mehak.flipkart.components.HeaderComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {
    private static final By LOGO = By.cssSelector("img[alt*='Flipkart']");
    private static final By POPUP_CLOSE = By.cssSelector("button._2KpZ6l._2doB4z");
    private static final By HOME_PAGE_CONTENT = By.cssSelector("body");

    private final HeaderComponent headerComponent;

    public HomePage(WebDriver driver) {
        super(driver);
        this.headerComponent = new HeaderComponent(driver);
    }

    public void open() {
        open("https://www.flipkart.com");
        closePopupIfPresent();
    }

    public boolean isLoaded() {
        return isDisplayed(LOGO) || isDisplayed(HOME_PAGE_CONTENT);
    }

    public void closePopupIfPresent() {
        try {
            click(POPUP_CLOSE);
        } catch (RuntimeException ignored) {
            // Flipkart popup may not be present; ignore gracefully.
        }
    }

    public HeaderComponent header() {
        return headerComponent;
    }
}
