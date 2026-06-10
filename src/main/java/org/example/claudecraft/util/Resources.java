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
        return new String(readBytes(path), StandardCharsets.UTF_8);
    }

    /**
     * Reads a classpath resource as raw bytes.
     *
     * @param path absolute classpath location, e.g. {@code /textures/atlas.png}
     * @throws ResourceLoadException if the resource is missing or unreadable
     */
    public static byte[] readBytes(String path) {
        Objects.requireNonNull(path, "path");
        try (InputStream in = Resources.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new ResourceLoadException("Resource not found on classpath: " + path);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new ResourceLoadException("Failed to read resource: " + path, e);
        }
    }
}
