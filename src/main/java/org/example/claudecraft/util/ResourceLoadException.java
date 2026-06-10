package org.example.claudecraft.util;

/** Thrown when a classpath resource (shader source, texture, …) cannot be located or read. */
public final class ResourceLoadException extends RuntimeException {

    public ResourceLoadException(String message) {
        super(message);
    }

    public ResourceLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
