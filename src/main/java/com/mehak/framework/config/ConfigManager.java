package com.mehak.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public final class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    private static final Properties PROPERTIES = new Properties();
    private static final String DEFAULT_CONFIG = "config/config.properties";

    private ConfigManager() {
    }

    public static void load() {
        PROPERTIES.clear();
        load(DEFAULT_CONFIG);

        String environment = get("environment", "qa");
        String envFile = "config/config-" + environment + ".properties";
        loadIfPresent(envFile);

        overrideWithSystemProperties();
        LOGGER.info("Configuration loaded for environment: {}", environment);
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String defaultValue) {
        if (PROPERTIES.isEmpty()) {
            load();
        }
        String value = System.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key, "false"));
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key, "0"));
    }

    public static long getLong(String key) {
        return Long.parseLong(get(key, "0"));
    }

    private static void load(String resourcePath) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                LOGGER.warn("Configuration file not found: {}", resourcePath);
                return;
            }
            PROPERTIES.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("Failed to load configuration file {}", resourcePath, e);
        }
    }

    private static void loadIfPresent(String resourcePath) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                Properties environmentProperties = new Properties();
                environmentProperties.load(inputStream);
                environmentProperties.forEach((key, value) -> PROPERTIES.setProperty(String.valueOf(key), String.valueOf(value)));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load environment configuration {}", resourcePath, e);
        }
    }

    private static void overrideWithSystemProperties() {
        for (String name : PROPERTIES.stringPropertyNames()) {
            String systemProperty = System.getProperty(name);
            if (systemProperty != null && !systemProperty.isBlank()) {
                PROPERTIES.setProperty(name, systemProperty);
            }
        }
    }

    public static String getBrowser() {
        return get("browser", "chrome").toLowerCase(Locale.ROOT);
    }

    public static boolean isHeadless() {
        return getBoolean("headless");
    }

    public static String getBaseUrl() {
        return get("baseUrl", "https://www.flipkart.com");
    }

    public static int getExplicitWait() {
        return getInt("explicitWait");
    }
}
