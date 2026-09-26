package com.sourav.framework.api;

import com.sourav.framework.config.ConfigManager;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class ApiRequestBuilder {
    private ApiRequestBuilder() {
    }

    public static RequestSpecification defaultSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.get("api.baseUri", "https://jsonplaceholder.typicode.com"))
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }
}
