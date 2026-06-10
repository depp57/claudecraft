package org.example.claudecraft.render;

import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_LINES;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL15C.glGenBuffers;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

/**
 * Draws a small screen-space crosshair as two white lines, sized relative to
 * screen height and corrected for aspect ratio.
 *
 * <p>Owns its GL objects; release them with {@link #close()}. Render thread
 * only.
 */
public final class CrosshairRenderer implements AutoCloseable {

    /** Crosshair half-extent as a fraction of half the screen height. */
    private static final float HALF_SIZE = 0.025f;

    private final ShaderProgram shader;
    private final int vaoId;
    private final int vertexBufferId;

    public CrosshairRenderer() {
        shader = ShaderProgram.load("/shaders/crosshair.vert", "/shaders/crosshair.frag");

        float[] vertices = {
                -1.0f, 0.0f, 1.0f, 0.0f,  // horizontal line
                0.0f, -1.0f, 0.0f, 1.0f,  // vertical line
        };
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
    }

    /** Draws the crosshair over the current frame; call after the world pass. */
    public void draw(float aspectRatio) {
        glDisable(GL_DEPTH_TEST);
        shader.bind();
        shader.setUniform("uScale", HALF_SIZE / aspectRatio, HALF_SIZE);
        glBindVertexArray(vaoId);
        glDrawArrays(GL_LINES, 0, 4);
        glBindVertexArray(0);
        shader.unbind();
        glEnable(GL_DEPTH_TEST);
    }

    @Override
    public void close() {
        glDeleteBuffers(vertexBufferId);
        glDeleteVertexArrays(vaoId);
        shader.close();
    }
}
