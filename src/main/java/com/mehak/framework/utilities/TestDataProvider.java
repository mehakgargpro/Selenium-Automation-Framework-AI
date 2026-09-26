package com.mehak.framework.utilities;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public final class TestDataProvider {
    private TestDataProvider() {
    }

    public static List<String> getSearchTerms() {
        JsonNode root = JsonUtil.readTree(ResourceLoader.readResourceAsString("testdata/products.json"));
        List<String> terms = new ArrayList<>();
        JsonNode termsNode = root.get("searchTerms");
        if (termsNode != null && termsNode.isArray()) {
            for (JsonNode node : termsNode) {
                terms.add(node.asText());
            }
        }
        return terms;
    }
}
