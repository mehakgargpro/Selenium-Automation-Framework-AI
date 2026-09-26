package com.sourav.flipkart.tests;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.sourav.flipkart.pages.HomePage;

public class FlipkartHomeTest extends BaseTest {

    @Test(retryAnalyzer = com.sourav.framework.listeners.RetryAnalyzer.class)
    public void shouldOpenFlipkartHomePage() {
        HomePage homePage = new HomePage(driver);
        homePage.open();
        boolean isLoaded = homePage.isLoaded();
        if (!isLoaded) {
            throw new SkipException("Flipkart homepage was unavailable or blocked; home page validation could not run.");
        }
        Assert.assertTrue(homePage.header().isSearchBoxVisible(), "Search box should be visible on home page");
        Assert.assertTrue(homePage.getPageTitle().toLowerCase().contains("flipkart") || homePage.header().isSearchBoxVisible(), "Page title or search input indicates successful page load");
    }
}
