package org.example.claudecraft.render;

import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.glDepthMask;
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
 * Procedural skybox: one fullscreen triangle whose fragments are turned into
 * view rays, shaded as a zenith→horizon gradient with a sun disc and halo.
 * Drawn first each frame with depth writes off so the world renders over it.
 *
 * <p>Owns its GL objects; release them with {@link #close()}. Render thread
 * only.
 */
public final class SkyRenderer implements AutoCloseable {

    private final ShaderProgram shader;
    private final int vaoId;
    private final int vertexBufferId;

    public SkyRenderer() {
        shader = ShaderProgram.load("/shaders/sky.vert", "/shaders/sky.frag");

        // One triangle covering the whole screen in NDC.
        float[] vertices = {-1.0f, -1.0f, 3.0f, -1.0f, -1.0f, 3.0f};
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
    }

    /**
     * Draws the sky.
     *
     * @param inverseViewProjection inverse of projection × rotation-only view
     * @param sunDirection          normalized direction the sunlight travels
     */
    public void draw(Matrix4fc inverseViewProjection, Vector3fc sunDirection,
                     Vector3fc zenithColor, Vector3fc horizonColor, float daylight) {
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);

        shader.bind();
        shader.setUniform("uInverseViewProjection", inverseViewProjection);
        shader.setUniform("uSunDirection", sunDirection);
        shader.setUniform("uZenithColor", zenithColor);
        shader.setUniform("uHorizonColor", horizonColor);
        shader.setUniform("uDaylight", daylight);
        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);
        shader.unbind();

        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
    }

    @Override
    public void close() {
        glDeleteBuffers(vertexBufferId);
        glDeleteVertexArrays(vaoId);
        shader.close();
    }
}
