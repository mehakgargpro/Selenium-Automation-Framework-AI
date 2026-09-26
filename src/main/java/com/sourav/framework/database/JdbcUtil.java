package com.sourav.framework.database;

import com.sourav.framework.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JdbcUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcUtil.class);

    private JdbcUtil() {
    }

    public static boolean isConfigured() {
        String url = ConfigManager.get("db.url", "").trim();
        return !url.isBlank();
    }

    public static Connection getConnection() throws SQLException {
        String url = ConfigManager.get("db.url", "");
        String username = ConfigManager.get("db.username", "");
        String password = ConfigManager.get("db.password", "");

        if (url.isBlank()) {
            throw new IllegalStateException("Database URL is not configured. Set db.url in config or system properties.");
        }

        return DriverManager.getConnection(url, username, password);
    }

    public static List<Map<String, Object>> executeQuery(String sql, Object... parameters) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public static int executeUpdate(String sql, Object... parameters) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            return statement.executeUpdate();
        }
    }

    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            LOGGER.warn("Unable to close resource cleanly", e);
        }
    }
}
