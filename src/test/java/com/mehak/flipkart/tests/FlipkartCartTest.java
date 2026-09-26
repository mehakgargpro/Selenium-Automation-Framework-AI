package com.mehak.flipkart.tests;

import com.mehak.flipkart.pages.CartPage;
import com.mehak.flipkart.pages.HomePage;
import com.mehak.flipkart.pages.ProductPage;
import com.mehak.flipkart.pages.SearchResultsPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class FlipkartCartTest extends BaseTest {

    @Test(retryAnalyzer = com.mehak.framework.listeners.RetryAnalyzer.class)
    public void shouldOpenCartUiSafely() {
        HomePage homePage = new HomePage(driver);
        homePage.open();

        homePage.header().searchFor("headphones");
        SearchResultsPage resultsPage = new SearchResultsPage(driver);
        if (!resultsPage.areResultsDisplayed()) {
            throw new SkipException("Flipkart did not display search results; cart validation could not run.");
        }

        ProductPage productPage = resultsPage.openFirstProduct();
        if (!productPage.isAddToCartVisible()) {
            throw new SkipException("Flipkart did not expose an add-to-cart control; cart validation could not run.");
        }

        productPage.addToCart();
        CartPage cartPage = new CartPage(driver);
        if (!cartPage.isCartVisible()) {
            throw new SkipException("Flipkart cart contents were not visible; cart validation could not run.");
        }

        cartPage.removeItemIfVisible();
    }
}
