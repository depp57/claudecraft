package org.example.claudecraft.world;

import org.example.claudecraft.world.gen.FlatTerrainGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamingWorldTest {

    private static final int GROUND = 64;
    private static final long TIMEOUT_MILLIS = 5_000;

    /** Records load/unload events from the world. */
    private static final class RecordingListener implements ChunkListener {
        final List<ChunkPos> loaded = new ArrayList<>();
        final List<ChunkPos> unloaded = new ArrayList<>();

        @Override
        public void onChunkLoaded(ChunkPos position) {
            loaded.add(position);
        }

        @Override
        public void onChunkUnloaded(ChunkPos position) {
            unloaded.add(position);
        }
    }

    private static void updateUntilLoaded(StreamingWorld world, ChunkPos center, int expectedChunks)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
        while (world.chunkPositions().size() < expectedChunks) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("timed out: only " + world.chunkPositions().size()
                        + " of " + expectedChunks + " chunks loaded");
            }
            world.update(center);
            Thread.sleep(5);
        }
    }

    @Test
    void streamsChunksAroundCenterAndNotifiesListener() throws InterruptedException {
        try (StreamingWorld world = new StreamingWorld(new FlatTerrainGenerator(GROUND), 1)) {
            RecordingListener listener = new RecordingListener();
            world.addListener(listener);

            updateUntilLoaded(world, new ChunkPos(0, 0), 9);

            assertEquals(9, listener.loaded.size());
            assertTrue(world.chunk(new ChunkPos(-1, 1)).isPresent());
            assertEquals(BlockType.GRASS, world.block(0, GROUND, 0));
            assertEquals(BlockType.GRASS, world.block(-16, GROUND, 31));
        }
    }

    @Test
    void unloadsChunksLeftBehindWithHysteresis() throws InterruptedException {
        try (StreamingWorld world = new StreamingWorld(new FlatTerrainGenerator(GROUND), 1)) {
            RecordingListener listener = new RecordingListener();
            world.addListener(listener);
            updateUntilLoaded(world, new ChunkPos(0, 0), 9);
            List<ChunkPos> initialChunks = List.copyOf(listener.loaded);

            // One chunk over: everything stays within radius + 1 hysteresis.
            world.update(new ChunkPos(1, 0));
            assertTrue(listener.unloaded.isEmpty());

            // Far away: all the original chunks drop out (chunks requested in
            // between may load and unload too, so compare against the set).
            world.update(new ChunkPos(100, 100));
            assertTrue(listener.unloaded.containsAll(initialChunks));
            assertTrue(world.chunk(new ChunkPos(0, 0)).isEmpty());
            assertEquals(BlockType.AIR, world.block(0, GROUND, 0));
        }
    }

    @Test
    void editsApplyToStreamedChunks() throws InterruptedException {
        try (StreamingWorld world = new StreamingWorld(new FlatTerrainGenerator(GROUND), 0)) {
            updateUntilLoaded(world, new ChunkPos(0, 0), 1);

            assertTrue(world.setBlock(5, GROUND + 1, 5, BlockType.STONE));
            assertEquals(BlockType.STONE, world.block(5, GROUND + 1, 5));
        }
    }
}
