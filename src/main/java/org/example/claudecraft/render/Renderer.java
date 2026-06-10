package org.example.claudecraft.render;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glEnable;

/**
 * Top-level renderer. For now it only clears the frame to a sky color;
 * chunk rendering, shaders and the camera will hook in here.
 *
 * <p>Must only be used on the render thread that owns the OpenGL context.
 */
public final class Renderer {

    private static final float SKY_RED = 0.47f;
    private static final float SKY_GREEN = 0.71f;
    private static final float SKY_BLUE = 0.99f;

    public Renderer() {
        glEnable(GL_DEPTH_TEST);
        glClearColor(SKY_RED, SKY_GREEN, SKY_BLUE, 1.0f);
    }

    /** Clears the color and depth buffers in preparation for a new frame. */
    public void beginFrame() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
}
