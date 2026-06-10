package org.example.claudecraft.core;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Keyboard and mouse input for a {@link Window}. Captures the cursor for
 * first-person mouse look (raw motion where supported) and exposes per-tick
 * cursor deltas.
 *
 * <p>Call {@link #beginTick()} once at the start of each simulation tick to
 * latch the mouse deltas. The cursor-position callback registered here is
 * freed by {@link Window#close()}. Render thread only.
 */
public final class Input {

    private final Window window;

    private double cursorX;
    private double cursorY;
    private double lastCursorX;
    private double lastCursorY;
    private boolean cursorSeen;
    private float mouseDeltaX;
    private float mouseDeltaY;

    public Input(Window window) {
        this.window = Objects.requireNonNull(window, "window");
        long handle = window.handle();

        glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        if (glfwRawMouseMotionSupported()) {
            glfwSetInputMode(handle, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
        }
        glfwSetCursorPosCallback(handle, (win, x, y) -> {
            if (!cursorSeen) {
                // Avoid a view jump from the cursor's arbitrary initial position.
                lastCursorX = x;
                lastCursorY = y;
                cursorSeen = true;
            }
            cursorX = x;
            cursorY = y;
        });
    }

    /** Latches mouse movement since the previous tick into the delta accessors. */
    public void beginTick() {
        mouseDeltaX = (float) (cursorX - lastCursorX);
        mouseDeltaY = (float) (cursorY - lastCursorY);
        lastCursorX = cursorX;
        lastCursorY = cursorY;
    }

    /** Horizontal cursor movement since the last tick, in pixels (positive = right). */
    public float mouseDeltaX() {
        return mouseDeltaX;
    }

    /** Vertical cursor movement since the last tick, in pixels (positive = down). */
    public float mouseDeltaY() {
        return mouseDeltaY;
    }

    /** Returns true while the given {@code GLFW_KEY_*} key is held down. */
    public boolean isKeyDown(int key) {
        return glfwGetKey(window.handle(), key) == GLFW_PRESS;
    }
}
