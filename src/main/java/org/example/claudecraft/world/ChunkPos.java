package org.example.claudecraft.world;

/** Position of a chunk in the world's chunk grid (world blocks / 16). */
public record ChunkPos(int x, int z) {

    /** The chunk containing the given world block column. */
    public static ChunkPos containing(int worldX, int worldZ) {
        return new ChunkPos(Math.floorDiv(worldX, Chunk.SIZE_X), Math.floorDiv(worldZ, Chunk.SIZE_Z));
    }

    /** World x of this chunk's westernmost blocks. */
    public int minBlockX() {
        return x * Chunk.SIZE_X;
    }

    /** World z of this chunk's northernmost blocks. */
    public int minBlockZ() {
        return z * Chunk.SIZE_Z;
    }
}
