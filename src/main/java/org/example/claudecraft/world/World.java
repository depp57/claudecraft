package org.example.claudecraft.world;

import java.util.Optional;
import java.util.Set;

/**
 * A collection of chunks addressed in world coordinates. Pure logic, no
 * OpenGL; rendering observes it through these accessors.
 */
public interface World {

    /**
     * The block at the given world position. Positions outside loaded chunks
     * or outside the vertical range are {@link BlockType#AIR}, never an error.
     */
    BlockType block(int worldX, int worldY, int worldZ);

    /** The chunk at the given grid position, if loaded. */
    Optional<Chunk> chunk(ChunkPos position);

    /** Grid positions of all loaded chunks. */
    Set<ChunkPos> chunkPositions();
}
