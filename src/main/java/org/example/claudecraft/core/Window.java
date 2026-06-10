package org.example.claudecraft.core;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Wraps a GLFW window with an OpenGL 3.3 core-profile context.
 *
 * <p>Owns the GLFW lifecycle: {@code glfwInit} happens in the constructor and
 * {@code glfwTerminate} in {@link #close()}, so at most one instance may exist
 * at a time. All methods must be called from the main (render) thread.
 */
public final class Window implements AutoCloseable {

    private final long handle;
    private final GLFWErrorCallback errorCallback;
    private int framebufferWidth;
    private int framebufferHeight;

    /**
     * Creates and shows a window with a current OpenGL context and vsync enabled.
     *
     * @param title  window title, must not be null
     * @param width  initial width in screen coordinates, must be positive
     * @param height initial height in screen coordinates, must be positive
     * @throws WindowCreationException if GLFW or the window cannot be initialized
     */
    public Window(String title, int width, int height) {
        Objects.requireNonNull(title, "title");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Window size must be positive: " + width + "x" + height);
        }

        errorCallback = GLFWErrorCallback.createPrint(System.err);
        glfwSetErrorCallback(errorCallback);
        if (!glfwInit()) {
            errorCallback.free();
            throw new WindowCreationException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) {
            glfwTerminate();
            errorCallback.free();
            throw new WindowCreationException("Failed to create GLFW window");
        }

        glfwMakeContextCurrent(handle);
        GL.createCapabilities();
        glfwSwapInterval(1);

        glfwSetFramebufferSizeCallback(handle, (win, newWidth, newHeight) -> {
            framebufferWidth = newWidth;
            framebufferHeight = newHeight;
            glViewport(0, 0, newWidth, newHeight);
        });
        int[] fbWidth = new int[1];
        int[] fbHeight = new int[1];
        glfwGetFramebufferSize(handle, fbWidth, fbHeight);
        framebufferWidth = fbWidth[0];
        framebufferHeight = fbHeight[0];

        glfwShowWindow(handle);
    }

    /** Returns true once the user has requested the window to close. */
    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    /** Flags the window to close; the game loop exits at the end of the current frame. */
    public void requestClose() {
        glfwSetWindowShouldClose(handle, true);
    }

    /** Swaps the back buffer and processes pending input events. Call once per frame. */
    public void update() {
        glfwSwapBuffers(handle);
        glfwPollEvents();
    }

    /** Native GLFW handle, for other core wrappers ({@link Input}) only. */
    long handle() {
        return handle;
    }

    public int framebufferWidth() {
        return framebufferWidth;
    }

    public int framebufferHeight() {
        return framebufferHeight;
    }

    @Override
    public void close() {
        Callbacks.glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
        glfwTerminate();
        errorCallback.free();
    }
}
