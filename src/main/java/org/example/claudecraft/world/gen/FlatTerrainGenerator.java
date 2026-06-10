package org.example.claudecraft.world.gen;

import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.Chunk;

/**
 * Generates flat terrain: a grass surface at a fixed height, a few layers of
 * dirt beneath it, and stone down to bedrock level. Placeholder until the
 * noise-based generator lands.
 */
public final class FlatTerrainGenerator implements TerrainGenerator {

    private static final int DIRT_LAYERS = 3;

    private final int groundHeight;

    /**
     * @param groundHeight y level of the grass surface, within chunk bounds
     */
    public FlatTerrainGenerator(int groundHeight) {
        if (groundHeight < 0 || groundHeight >= Chunk.SIZE_Y) {
            throw new IllegalArgumentException("groundHeight out of range [0, " + Chunk.SIZE_Y + "): " + groundHeight);
        }
        this.groundHeight = groundHeight;
    }

    @Override
    public void generate(Chunk chunk) {
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                for (int y = 0; y <= groundHeight; y++) {
                    chunk.setBlock(x, y, z, typeAt(y));
                }
            }
        }
    }

    private BlockType typeAt(int y) {
        if (y == groundHeight) {
            return BlockType.GRASS;
        }
        if (y >= groundHeight - DIRT_LAYERS) {
            return BlockType.DIRT;
        }
        return BlockType.STONE;
    }
}
