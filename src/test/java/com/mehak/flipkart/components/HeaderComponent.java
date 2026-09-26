package com.mehak.flipkart.components;

import com.mehak.flipkart.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HeaderComponent extends BasePage {
    private static final By SEARCH_BOX = By.cssSelector("input[title='Search for Products, Brands and More']");
    private static final By SEARCH_BUTTON = By.cssSelector("button[type='submit']");
    private static final By CART_BUTTON = By.cssSelector("a[href*='/viewcart']");
    private static final By LOGIN_BUTTON = By.cssSelector("a[href*='login']");

    public HeaderComponent(WebDriver driver) {
        super(driver);
    }

    public void searchFor(String product) {
        type(SEARCH_BOX, product);
        clickJs(SEARCH_BUTTON);
    }

    public boolean isSearchBoxVisible() {
        return isDisplayed(SEARCH_BOX);
    }

    public boolean isCartAvailable() {
        return isDisplayed(CART_BUTTON);
    }

    public boolean isLoginVisible() {
        return isDisplayed(LOGIN_BUTTON);
    }
}
