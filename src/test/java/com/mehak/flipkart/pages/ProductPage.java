package com.mehak.flipkart.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductPage extends BasePage {
    private static final By PRODUCT_NAME = By.cssSelector("h1, h2, span._35KyD6");
    private static final By PRODUCT_PRICE = By.cssSelector("div._30jeq3, div._1vC4OE");
    private static final By BUY_NOW_BUTTON = By.cssSelector("button, a[href*='buy']");
    private static final By ADD_TO_CART_BUTTON = By.cssSelector("button, a[href*='cart']");

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public boolean isProductDetailsVisible() {
        return isDisplayed(PRODUCT_NAME) || isDisplayed(PRODUCT_PRICE);
    }

    public String getProductTitle() {
        return getText(PRODUCT_NAME);
    }

    public String getProductPrice() {
        return getText(PRODUCT_PRICE);
    }

    public boolean isAddToCartVisible() {
        return isDisplayed(ADD_TO_CART_BUTTON);
    }

    public void addToCart() {
        click(ADD_TO_CART_BUTTON);
    }
}
