package com.sourav.framework.api;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApiResponseUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiResponseUtils.class);

    private ApiResponseUtils() {
    }

    public static void assertStatusCode(Response response, int expectedStatusCode) {
        int actual = response.getStatusCode();
        if (actual != expectedStatusCode) {
            LOGGER.error("Unexpected status code. Expected {} but got {}. Response body: {}", expectedStatusCode, actual, response.asString());
            throw new AssertionError("Expected status code " + expectedStatusCode + " but got " + actual);
        }
    }

    public static void assertJsonPath(Response response, String jsonPath, String expectedValue) {
        String actual = response.jsonPath().getString(jsonPath);
        if (actual == null || !actual.equals(expectedValue)) {
            throw new AssertionError("Expected JSON path '" + jsonPath + "' to equal '" + expectedValue + "' but got '" + actual + "'");
        }
    }
}
