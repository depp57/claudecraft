package org.example.claudecraft.render;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

/**
 * An indexed triangle mesh on the GPU (VAO + vertex/index buffers).
 *
 * <p>Vertex layout is interleaved {@code vec3 position, vec2 texCoord,
 * vec3 normal}, matching attribute locations 0–2 of {@code chunk.vert}.
 *
 * <p>Owns its GL objects; release them with {@link #close()}. Render thread only.
 */
public final class Mesh implements AutoCloseable {

    private static final int FLOATS_PER_VERTEX = 8;

    private final int vaoId;
    private final int vertexBufferId;
    private final int indexBufferId;
    private final int indexCount;

    /**
     * Uploads the given geometry with {@code GL_STATIC_DRAW} usage.
     *
     * @param vertices interleaved vertex data, {@value FLOATS_PER_VERTEX} floats per vertex
     * @param indices  triangle indices into {@code vertices}, length a multiple of 3
     */
    public Mesh(float[] vertices, int[] indices) {
        if (vertices.length == 0 || vertices.length % FLOATS_PER_VERTEX != 0) {
            throw new IllegalArgumentException(
                    "Vertex data length must be a positive multiple of " + FLOATS_PER_VERTEX + ", got " + vertices.length);
        }
        if (indices.length == 0 || indices.length % 3 != 0) {
            throw new IllegalArgumentException(
                    "Index count must be a positive multiple of 3, got " + indices.length);
        }
        indexCount = indices.length;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int strideBytes = FLOATS_PER_VERTEX * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, strideBytes, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, strideBytes, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, strideBytes, 5L * Float.BYTES);
        glEnableVertexAttribArray(2);

        indexBufferId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glBindVertexArray(0);
    }

    /** Draws the mesh as triangles. A shader program must be bound. */
    public void draw() {
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    @Override
    public void close() {
        glDeleteBuffers(vertexBufferId);
        glDeleteBuffers(indexBufferId);
        glDeleteVertexArrays(vaoId);
    }
}
