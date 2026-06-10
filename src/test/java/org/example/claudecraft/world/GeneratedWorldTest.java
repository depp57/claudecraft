package org.example.claudecraft.world;

import org.example.claudecraft.world.gen.FlatTerrainGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratedWorldTest {

    private static final int GROUND = 64;

    private final GeneratedWorld world = new GeneratedWorld(new FlatTerrainGenerator(GROUND), 1);

    @Test
    void generatesFullSquareGrid() {
        assertEquals(9, world.chunkPositions().size());
        assertTrue(world.chunk(new ChunkPos(-1, 1)).isPresent());
        assertFalse(world.chunk(new ChunkPos(2, 0)).isPresent());
    }

    @Test
    void blockLookupSpansChunksIncludingNegativeCoordinates() {
        assertEquals(BlockType.GRASS, world.block(0, GROUND, 0));
        assertEquals(BlockType.GRASS, world.block(-16, GROUND, 31));
        assertEquals(BlockType.DIRT, world.block(-1, GROUND - 1, -1));
        assertEquals(BlockType.AIR, world.block(5, GROUND + 1, 5));
    }

    @Test
    void outsideLoadedAreaAndVerticalRangeIsAir() {
        assertEquals(BlockType.AIR, world.block(1000, GROUND, 0));
        assertEquals(BlockType.AIR, world.block(0, -1, 0));
        assertEquals(BlockType.AIR, world.block(0, Chunk.SIZE_Y, 0));
    }

    @Test
    void setBlockEditsLoadedChunks() {
        assertTrue(world.setBlock(-5, GROUND + 5, 20, BlockType.STONE));
        assertEquals(BlockType.STONE, world.block(-5, GROUND + 5, 20));

        assertTrue(world.setBlock(-5, GROUND + 5, 20, BlockType.AIR));
        assertEquals(BlockType.AIR, world.block(-5, GROUND + 5, 20));
    }

    @Test
    void setBlockRejectsNoOpsAndUnloadedPositions() {
        assertFalse(world.setBlock(0, GROUND, 0, BlockType.GRASS), "same type is a no-op");
        assertFalse(world.setBlock(1000, GROUND, 0, BlockType.STONE), "outside loaded chunks");
        assertFalse(world.setBlock(0, -1, 0, BlockType.STONE), "below the world");
        assertFalse(world.setBlock(0, Chunk.SIZE_Y, 0, BlockType.STONE), "above the world");
    }
}
