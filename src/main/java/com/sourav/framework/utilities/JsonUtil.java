package com.sourav.framework.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class JsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtil() {
    }

    public static <T> T readValue(String json, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(json, valueType);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse JSON", e);
        }
    }

    public static <T> T readValue(InputStream inputStream, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(inputStream, valueType);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse JSON stream", e);
        }
    }

    public static <T> T readValue(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON into type reference", e);
        }
    }

    public static JsonNode readTree(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse JSON tree", e);
        }
    }

    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert object to JSON", e);
        }
    }

    public static List<String> readStringList(String resourcePath) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            return OBJECT_MAPPER.readValue(stream, new TypeReference<>() {
            });
        } catch (IOException e) {
            LOGGER.error("Failed to read resource {}", resourcePath, e);
            throw new IllegalArgumentException("Unable to read resource: " + resourcePath, e);
        }
    }

    public static Map<String, Object> readMap(String resourcePath) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            return OBJECT_MAPPER.readValue(stream, new TypeReference<>() {
            });
        } catch (IOException e) {
            LOGGER.error("Failed to read resource {}", resourcePath, e);
            throw new IllegalArgumentException("Unable to read resource: " + resourcePath, e);
        }
    }
}
