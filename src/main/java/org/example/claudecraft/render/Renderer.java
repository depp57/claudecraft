package org.example.claudecraft.render;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glEnable;

/**
 * Top-level renderer owning the shader pipeline. Currently draws a single
 * vertex-colored quad to prove the GLSL path end to end; chunk rendering and
 * the camera will hook in here.
 *
 * <p>Owns GL resources; release them with {@link #close()}. Render thread only.
 */
public final class Renderer implements AutoCloseable {

    private static final float SKY_RED = 0.47f;
    private static final float SKY_GREEN = 0.71f;
    private static final float SKY_BLUE = 0.99f;

    private final ShaderProgram shader;
    private final Mesh demoQuad;

    public Renderer() {
        glEnable(GL_DEPTH_TEST);
        glClearColor(SKY_RED, SKY_GREEN, SKY_BLUE, 1.0f);

        shader = ShaderProgram.load("/shaders/basic.vert", "/shaders/basic.frag");
        demoQuad = new Mesh(
                new float[] {
                        // position             color
                        -0.5f, -0.5f, 0.0f,     1.0f, 0.0f, 0.0f,
                         0.5f, -0.5f, 0.0f,     0.0f, 1.0f, 0.0f,
                         0.5f,  0.5f, 0.0f,     0.0f, 0.0f, 1.0f,
                        -0.5f,  0.5f, 0.0f,     1.0f, 1.0f, 0.0f,
                },
                new int[] {0, 1, 2, 2, 3, 0});
    }

    /** Renders one frame. */
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shader.bind();
        demoQuad.draw();
        shader.unbind();
    }

    @Override
    public void close() {
        demoQuad.close();
        shader.close();
    }
}
