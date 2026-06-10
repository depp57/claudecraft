package org.example.claudecraft.render.chunk;

import org.example.claudecraft.world.Chunk;

/**
 * Turns chunk block data into mesh geometry. Implementations are pure CPU
 * logic (no OpenGL calls), so they can run on worker threads and be unit
 * tested headless.
 */
public interface ChunkMesher {

    /** Builds the geometry for the given chunk. */
    MeshData mesh(Chunk chunk);
}
