package org.example.claudecraft.world.gen;

import org.example.claudecraft.world.Chunk;

/**
 * Fills chunks with terrain. Implementations must be deterministic for a given
 * seed/configuration and stateless per call, so they can run on worker threads
 * (one chunk per call; the chunk must not be shared while generating).
 */
public interface TerrainGenerator {

    /** Populates the given (typically empty) chunk with blocks. */
    void generate(Chunk chunk);
}
