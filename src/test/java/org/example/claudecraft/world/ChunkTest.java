package org.example.claudecraft.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkTest {

    @Test
    void newChunkIsAllAir() {
        Chunk chunk = new Chunk();
        assertEquals(BlockType.AIR, chunk.block(0, 0, 0));
        assertEquals(BlockType.AIR, chunk.block(15, 255, 15));
        assertEquals(BlockType.AIR, chunk.block(7, 100, 9));
    }

    @Test
    void setBlockRoundTrips() {
        Chunk chunk = new Chunk();
        chunk.setBlock(3, 200, 12, BlockType.STONE);
        assertEquals(BlockType.STONE, chunk.block(3, 200, 12));
        assertEquals(BlockType.AIR, chunk.block(3, 201, 12));

        chunk.setBlock(3, 200, 12, BlockType.AIR);
        assertEquals(BlockType.AIR, chunk.block(3, 200, 12));
    }

    @Test
    void outOfBoundsAccessThrows() {
        Chunk chunk = new Chunk();
        assertThrows(IndexOutOfBoundsException.class, () -> chunk.block(-1, 0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> chunk.block(16, 0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> chunk.block(0, 256, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> chunk.block(0, 0, 16));
        assertThrows(IndexOutOfBoundsException.class, () -> chunk.setBlock(0, -1, 0, BlockType.STONE));
    }

    @Test
    void containsMatchesBounds() {
        assertTrue(Chunk.contains(0, 0, 0));
        assertTrue(Chunk.contains(15, 255, 15));
        assertFalse(Chunk.contains(-1, 0, 0));
        assertFalse(Chunk.contains(0, 256, 0));
        assertFalse(Chunk.contains(0, 0, 16));
    }
}
