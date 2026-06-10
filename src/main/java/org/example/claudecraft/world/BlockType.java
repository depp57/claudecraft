package org.example.claudecraft.world;

/**
 * All block kinds in the game. The ordinal doubles as the storage id inside
 * {@link Chunk}, so only append new types — never reorder.
 */
public enum BlockType {
    AIR(false),
    GRASS(true),
    DIRT(true),
    STONE(true);

    private final boolean opaque;

    BlockType(boolean opaque) {
        this.opaque = opaque;
    }

    /** Opaque blocks hide the faces of their neighbors; air and (later) glass/water do not. */
    public boolean isOpaque() {
        return opaque;
    }

    /** Solid blocks take part in collision and block rays; only air (and later fluids) do not. */
    public boolean isSolid() {
        return this != AIR;
    }
}
