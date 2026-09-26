package com.mehak.framework.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ResourceLoader {
    private ResourceLoader() {
    }

    public static String readResourceAsString(String resourcePath) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read resource: " + resourcePath, e);
        }
    }
}
