package org.example.claudecraft.render.chunk;

import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.Direction;

/**
 * Maps block faces to atlas tiles. Tile positions must match the layout
 * produced by {@code tools/AtlasGen.java}. Pure lookup, no GL.
 */
public final class BlockTextures {

    /** The atlas is a square grid of this many tiles per side. */
    public static final int TILES_PER_ROW = 4;

    private static final TextureTile GRASS_TOP = TextureTile.of(0, 0, TILES_PER_ROW);
    private static final TextureTile GRASS_SIDE = TextureTile.of(1, 0, TILES_PER_ROW);
    private static final TextureTile DIRT = TextureTile.of(2, 0, TILES_PER_ROW);
    private static final TextureTile STONE = TextureTile.of(3, 0, TILES_PER_ROW);

    private BlockTextures() {
    }

    /**
     * Returns the atlas tile for the given face of the given block.
     *
     * @throws IllegalArgumentException for {@link BlockType#AIR}, which has no faces
     */
    public static TextureTile tileFor(BlockType block, Direction face) {
        return switch (block) {
            case GRASS -> switch (face) {
                case UP -> GRASS_TOP;
                case DOWN -> DIRT;
                case NORTH, SOUTH, WEST, EAST -> GRASS_SIDE;
            };
            case DIRT -> DIRT;
            case STONE -> STONE;
            case AIR -> throw new IllegalArgumentException("AIR has no faces to texture");
        };
    }
}
