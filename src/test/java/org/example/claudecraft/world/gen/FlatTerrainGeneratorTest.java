package org.example.claudecraft.world.gen;

import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.Chunk;
import org.example.claudecraft.world.ChunkPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlatTerrainGeneratorTest {

    private static final int GROUND = 64;

    @Test
    void generatesLayersGrassDirtStone() {
        Chunk chunk = new Chunk();
        new FlatTerrainGenerator(GROUND).generate(chunk, new ChunkPos(0, 0));

        assertEquals(BlockType.AIR, chunk.block(5, GROUND + 1, 5));
        assertEquals(BlockType.GRASS, chunk.block(5, GROUND, 5));
        assertEquals(BlockType.DIRT, chunk.block(5, GROUND - 1, 5));
        assertEquals(BlockType.DIRT, chunk.block(5, GROUND - 3, 5));
        assertEquals(BlockType.STONE, chunk.block(5, GROUND - 4, 5));
        assertEquals(BlockType.STONE, chunk.block(5, 0, 5));
    }

    @Test
    void coversWholeChunkFootprint() {
        Chunk chunk = new Chunk();
        new FlatTerrainGenerator(GROUND).generate(chunk, new ChunkPos(0, 0));

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                assertEquals(BlockType.GRASS, chunk.block(x, GROUND, z),
                        "expected grass surface at (" + x + ", " + GROUND + ", " + z + ")");
            }
        }
    }

    @Test
    void rejectsOutOfRangeGroundHeight() {
        assertThrows(IllegalArgumentException.class, () -> new FlatTerrainGenerator(-1));
        assertThrows(IllegalArgumentException.class, () -> new FlatTerrainGenerator(Chunk.SIZE_Y));
    }
}
