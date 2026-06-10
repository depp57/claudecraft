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

    /**
     * Replaces the block at the given world position.
     *
     * @return true if the position lies in a loaded chunk and the block
     *         actually changed; false for out-of-world positions or no-ops
     */
    boolean setBlock(int worldX, int worldY, int worldZ, BlockType type);

    /** The chunk at the given grid position, if loaded. */
    Optional<Chunk> chunk(ChunkPos position);

    /** Grid positions of all loaded chunks. */
    Set<ChunkPos> chunkPositions();
}
