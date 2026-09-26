package com.mehak.flipkart.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CartPage extends BasePage {
    private static final By CART_ITEM = By.cssSelector("div._1AtVbE, div._1_3w1N");
    private static final By REMOVE_BUTTON = By.cssSelector("button[aria-label='Remove']");
    private static final By EMPTY_CART_TEXT = By.cssSelector("div._1LCJ8C");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public boolean isCartVisible() {
        return isDisplayed(CART_ITEM) || isDisplayed(EMPTY_CART_TEXT);
    }

    public boolean removeItemIfVisible() {
        if (isDisplayed(REMOVE_BUTTON)) {
            click(REMOVE_BUTTON);
            return true;
        }
        return false;
    }
}
