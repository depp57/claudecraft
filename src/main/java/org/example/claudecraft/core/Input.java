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

    /** Buttons with edge detection: {@code GLFW_MOUSE_BUTTON_LEFT} and {@code _RIGHT}. */
    private static final int TRACKED_BUTTONS = 2;
    /** Keys with edge detection; extend when more just-pressed keys are needed. */
    private static final int[] TRACKED_KEYS = {GLFW_KEY_F5};

    private final Window window;

    private double cursorX;
    private double cursorY;
    private double lastCursorX;
    private double lastCursorY;
    private boolean cursorSeen;
    private float mouseDeltaX;
    private float mouseDeltaY;
    private final boolean[] buttonDown = new boolean[TRACKED_BUTTONS];
    private final boolean[] buttonJustPressed = new boolean[TRACKED_BUTTONS];
    private final boolean[] keyWasDown = new boolean[TRACKED_KEYS.length];
    private final boolean[] keyJustPressed = new boolean[TRACKED_KEYS.length];

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

    /** Latches mouse movement and button edges since the previous tick. */
    public void beginTick() {
        mouseDeltaX = (float) (cursorX - lastCursorX);
        mouseDeltaY = (float) (cursorY - lastCursorY);
        lastCursorX = cursorX;
        lastCursorY = cursorY;

        for (int button = 0; button < TRACKED_BUTTONS; button++) {
            boolean down = glfwGetMouseButton(window.handle(), button) == GLFW_PRESS;
            buttonJustPressed[button] = down && !buttonDown[button];
            buttonDown[button] = down;
        }
        for (int i = 0; i < TRACKED_KEYS.length; i++) {
            boolean down = isKeyDown(TRACKED_KEYS[i]);
            keyJustPressed[i] = down && !keyWasDown[i];
            keyWasDown[i] = down;
        }
    }

    /**
     * Returns true if the given key went from released to pressed since the
     * previous tick. Only keys listed in {@code TRACKED_KEYS} are supported.
     */
    public boolean isKeyJustPressed(int key) {
        for (int i = 0; i < TRACKED_KEYS.length; i++) {
            if (TRACKED_KEYS[i] == key) {
                return keyJustPressed[i];
            }
        }
        throw new IllegalArgumentException("Key not tracked for edge detection: " + key);
    }

    /**
     * Returns true if the given {@code GLFW_MOUSE_BUTTON_*} button went from
     * released to pressed since the previous tick.
     */
    public boolean isButtonJustPressed(int button) {
        Objects.checkIndex(button, TRACKED_BUTTONS);
        return buttonJustPressed[button];
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
