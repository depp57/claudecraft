package org.example.claudecraft.core;

/** Thrown when GLFW initialization or window creation fails. */
public final class WindowCreationException extends RuntimeException {

    public WindowCreationException(String message) {
        super(message);
    }
}
