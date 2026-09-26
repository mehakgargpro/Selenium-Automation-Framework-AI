package com.mehak.flipkart.pages;

import com.mehak.flipkart.components.ProductCardComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SearchResultsPage extends BasePage {
    private static final By RESULT_COUNT = By.cssSelector("span._10Ermr, span._2yAnYN");
    private static final By FILTER_PANEL = By.cssSelector("div._166f5");
    private static final By SORT_SELECT = By.cssSelector("select");
    private static final By PRODUCT_CARD = By.cssSelector("a[href*='/p/']");
    private static final By FIRST_PRODUCT = By.cssSelector("a[href*='/p/']");

    private final ProductCardComponent productCardComponent;

    public SearchResultsPage(WebDriver driver) {
        super(driver);
        this.productCardComponent = new ProductCardComponent(driver);
    }

    public boolean areResultsDisplayed() {
        return isDisplayed(PRODUCT_CARD);
    }

    public List<String> getResultTitles() {
        return productCardComponent.getProductCards().stream().map(WebElement::getText).limit(5).toList();
    }

    public ProductPage openFirstProduct() {
        WebElement firstProduct = find(FIRST_PRODUCT);
        firstProduct.click();
        return new ProductPage(driver);
    }

    public boolean hasFilterSection() {
        return isDisplayed(FILTER_PANEL);
    }

    public boolean canApplySorting() {
        return !findAll(SORT_SELECT).isEmpty();
    }
}
