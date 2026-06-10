package org.example.claudecraft.render.chunk;

import java.util.Objects;

/**
 * CPU-side mesh geometry produced by a {@link ChunkMesher}, ready to be
 * uploaded into a {@code Mesh} on the render thread. Layout matches
 * {@code Mesh}: interleaved {@code vec3 position, vec3 color}.
 *
 * <p>Note: record equality is by array identity, not content; this type is a
 * data carrier, not a value to compare.
 */
public record MeshData(float[] vertices, int[] indices) {

    public MeshData {
        Objects.requireNonNull(vertices, "vertices");
        Objects.requireNonNull(indices, "indices");
    }

    public boolean isEmpty() {
        return indices.length == 0;
    }
}
