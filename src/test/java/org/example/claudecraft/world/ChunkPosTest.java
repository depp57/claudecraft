package org.example.claudecraft.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChunkPosTest {

    @Test
    void containingFloorsTowardNegativeInfinity() {
        assertEquals(new ChunkPos(0, 0), ChunkPos.containing(0, 0));
        assertEquals(new ChunkPos(0, 0), ChunkPos.containing(15, 15));
        assertEquals(new ChunkPos(1, 0), ChunkPos.containing(16, 5));
        assertEquals(new ChunkPos(-1, -1), ChunkPos.containing(-1, -16));
        assertEquals(new ChunkPos(-2, -1), ChunkPos.containing(-17, -1));
    }

    @Test
    void minBlockCoordinatesScaleByChunkSize() {
        assertEquals(32, new ChunkPos(2, -3).minBlockX());
        assertEquals(-48, new ChunkPos(2, -3).minBlockZ());
    }
}
