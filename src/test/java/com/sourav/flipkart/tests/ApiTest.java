package com.sourav.flipkart.tests;

import com.sourav.framework.api.ApiClient;
import com.sourav.framework.api.ApiResponseUtils;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ApiTest extends BaseTest {

    @Test(retryAnalyzer = com.sourav.framework.listeners.RetryAnalyzer.class)
    public void shouldCallPublicApiSuccessfully() {
        ApiClient apiClient = new ApiClient();
        Response response = apiClient.get("/todos/1");
        ApiResponseUtils.assertStatusCode(response, 200);
        Assert.assertFalse(response.jsonPath().getString("title").isBlank(), "Public API title should be present");
    }
}
