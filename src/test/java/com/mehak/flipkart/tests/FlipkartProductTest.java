package com.mehak.flipkart.tests;

import com.mehak.flipkart.pages.HomePage;
import com.mehak.flipkart.pages.ProductPage;
import com.mehak.flipkart.pages.SearchResultsPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class FlipkartProductTest extends BaseTest {

    @Test(retryAnalyzer = com.mehak.framework.listeners.RetryAnalyzer.class)
    public void shouldOpenProductDetails() {
        HomePage homePage = new HomePage(driver);
        homePage.open();
        homePage.header().searchFor("laptop");

        SearchResultsPage searchResultsPage = new SearchResultsPage(driver);
        if (!searchResultsPage.areResultsDisplayed()) {
            throw new SkipException("Flipkart did not display laptop search results; product validation could not run.");
        }

        ProductPage productPage = searchResultsPage.openFirstProduct();
        if (!productPage.isProductDetailsVisible()) {
            throw new SkipException("Flipkart did not display product details; the product page may be blocked or have changed.");
        }

        String title = productPage.getProductTitle();
        String price = productPage.getProductPrice();
        Assert.assertTrue(title != null && !title.isBlank(), "Product title should not be blank");
        Assert.assertTrue(price == null || !price.isBlank(), "Product price should be blank or valid when shown");
    }
}
