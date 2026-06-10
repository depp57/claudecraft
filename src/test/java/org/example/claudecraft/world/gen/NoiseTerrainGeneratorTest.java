package org.example.claudecraft.world.gen;

import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.Chunk;
import org.example.claudecraft.world.ChunkPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoiseTerrainGeneratorTest {

    private static final long SEED = 12345L;
    private static final ChunkPos ORIGIN = new ChunkPos(0, 0);

    @Test
    void sameSeedIsDeterministic() {
        Chunk first = new Chunk();
        Chunk second = new Chunk();
        new NoiseTerrainGenerator(SEED).generate(first, ORIGIN);
        new NoiseTerrainGenerator(SEED).generate(second, ORIGIN);

        assertEquals(heightmap(first), heightmap(second));
    }

    @Test
    void differentSeedsProduceDifferentTerrain() {
        Chunk first = new Chunk();
        Chunk second = new Chunk();
        new NoiseTerrainGenerator(SEED).generate(first, ORIGIN);
        new NoiseTerrainGenerator(SEED + 1).generate(second, ORIGIN);

        assertNotEquals(heightmap(first), heightmap(second));
    }

    @Test
    void everyColumnIsGrassOverDirtOverStone() {
        Chunk chunk = new Chunk();
        new NoiseTerrainGenerator(SEED).generate(chunk, new ChunkPos(3, -2));

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                int surface = surfaceOf(chunk, x, z);
                assertTrue(surface > 0, "column (" + x + ", " + z + ") has no solid block");
                assertEquals(BlockType.GRASS, chunk.block(x, surface, z));
                assertEquals(BlockType.DIRT, chunk.block(x, surface - 1, z));
                assertEquals(BlockType.STONE, chunk.block(x, surface - 4, z));
                assertEquals(BlockType.STONE, chunk.block(x, 0, z));
            }
        }
    }

    @Test
    void terrainIsSeamlessAcrossChunkBorders() {
        NoiseTerrainGenerator generator = new NoiseTerrainGenerator(SEED);
        Chunk west = new Chunk();
        Chunk east = new Chunk();
        generator.generate(west, new ChunkPos(0, 0));
        generator.generate(east, new ChunkPos(1, 0));

        // Heights across the shared border may differ by at most the slope of
        // one block step; a seam from inconsistent sampling would show a jump.
        for (int z = 0; z < Chunk.SIZE_Z; z++) {
            int westEdge = surfaceOf(west, Chunk.SIZE_X - 1, z);
            int eastEdge = surfaceOf(east, 0, z);
            assertTrue(Math.abs(westEdge - eastEdge) <= 3,
                    "seam at z=" + z + ": " + westEdge + " vs " + eastEdge);
        }
    }

    private static java.util.List<Integer> heightmap(Chunk chunk) {
        java.util.List<Integer> heights = new java.util.ArrayList<>();
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                heights.add(surfaceOf(chunk, x, z));
            }
        }
        return heights;
    }

    private static int surfaceOf(Chunk chunk, int x, int z) {
        for (int y = Chunk.SIZE_Y - 1; y >= 0; y--) {
            if (chunk.block(x, y, z) != BlockType.AIR) {
                return y;
            }
        }
        return -1;
    }
}
