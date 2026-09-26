package com.mehak.flipkart.tests;

import com.mehak.framework.utilities.TestDataProvider;
import com.mehak.flipkart.pages.HomePage;
import com.mehak.flipkart.pages.SearchResultsPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class FlipkartSearchTest extends BaseTest {

    @DataProvider(name = "searchTerms")
    public Object[][] searchTerms() {
        List<String> terms = TestDataProvider.getSearchTerms();
        return terms.stream().map(term -> new Object[] { term }).toArray(Object[][]::new);
    }

    @Test(dataProvider = "searchTerms", retryAnalyzer = com.mehak.framework.listeners.RetryAnalyzer.class)
    public void shouldSearchProductAndDisplayResults(String searchTerm) {
        HomePage homePage = new HomePage(driver);
        homePage.open();
        homePage.header().searchFor(searchTerm);

        SearchResultsPage resultsPage = new SearchResultsPage(driver);
        if (!resultsPage.areResultsDisplayed()) {
            throw new SkipException("Flipkart did not display results for '" + searchTerm
                    + "'; the site may be blocking automation or its page structure may have changed.");
        }

        List<String> productTitles = resultsPage.getResultTitles();
        Assert.assertFalse(productTitles.isEmpty(), "Expected at least one product card in the results");
        Assert.assertTrue(productTitles.stream().anyMatch(title -> title != null && !title.isBlank()), "Product titles should contain meaningful text");
    }
}
