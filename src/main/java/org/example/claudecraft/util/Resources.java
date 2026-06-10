package org.example.claudecraft.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Loads bundled game assets from the classpath. */
public final class Resources {

    private Resources() {
    }

    /**
     * Reads a classpath resource as a UTF-8 string.
     *
     * @param path absolute classpath location, e.g. {@code /shaders/basic.vert}
     * @throws ResourceLoadException if the resource is missing or unreadable
     */
    public static String readString(String path) {
        Objects.requireNonNull(path, "path");
        try (InputStream in = Resources.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new ResourceLoadException("Resource not found on classpath: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ResourceLoadException("Failed to read resource: " + path, e);
        }
    }
}
