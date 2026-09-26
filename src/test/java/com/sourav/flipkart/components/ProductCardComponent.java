package com.sourav.flipkart.components;

import com.sourav.flipkart.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ProductCardComponent extends BasePage {
    private static final By PRODUCT_LINKS = By.cssSelector("a[href*='/p/'], div[data-id]");
    private static final By PRODUCT_NAME = By.cssSelector("div._4rR01T, a[href*='/p/']");
    private static final By PRODUCT_PRICE = By.cssSelector("div._30jeq3, div._1vC4OE");

    public ProductCardComponent(WebDriver driver) {
        super(driver);
    }

    public List<WebElement> getProductCards() {
        return findAll(PRODUCT_LINKS);
    }

    public WebElement getFirstProductCard() {
        return findAll(PRODUCT_LINKS).stream().findFirst().orElseThrow(() -> new IllegalStateException("No product cards found."));
    }

    public String getFirstProductTitle() {
        List<WebElement> cards = findAll(PRODUCT_LINKS);
        return cards.stream().findFirst().map(WebElement::getText).orElse("");
    }
}
