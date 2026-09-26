package com.sourav.flipkart.tests;

import com.sourav.framework.database.JdbcUtil;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class DatabaseTest extends BaseTest {

    @Test(retryAnalyzer = com.sourav.framework.listeners.RetryAnalyzer.class)
    public void shouldUseDatabaseUtilityWhenConfigured() throws Exception {
        if (!JdbcUtil.isConfigured()) {
            throw new SkipException("Database is not configured for this environment; skipping database validation gracefully.");
        }

        List<Map<String, Object>> rows = JdbcUtil.executeQuery("SELECT 1 AS result");
        Assert.assertFalse(rows.isEmpty(), "Expected one row from a simple database query");
        Assert.assertEquals(String.valueOf(rows.get(0).get("RESULT")), "1");
    }
}
