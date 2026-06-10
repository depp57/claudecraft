package org.example.claudecraft.world;

import org.example.claudecraft.world.gen.TerrainGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A world that streams chunks around a moving center: generation runs on a
 * worker pool, finished chunks are published and {@link ChunkListener}s
 * notified during {@link #update}, and chunks left behind are unloaded with
 * one chunk of hysteresis.
 *
 * <p>Thread ownership: all {@link World} methods, {@link #update} and
 * listener callbacks belong to the main thread. Workers only touch a chunk
 * before it is published, so generation needs no locking.
 */
public final class StreamingWorld implements World, AutoCloseable {

    private final TerrainGenerator generator;
    private final int loadRadius;
    private final Map<ChunkPos, Chunk> chunks = new HashMap<>();
    private final Set<ChunkPos> pending = new HashSet<>();
    private final ConcurrentLinkedQueue<GeneratedChunk> completed = new ConcurrentLinkedQueue<>();
    private final List<ChunkListener> listeners = new ArrayList<>();
    private final ExecutorService generationPool;

    private record GeneratedChunk(ChunkPos position, Chunk chunk) {
    }

    /**
     * @param loadRadius chunks are kept loaded within this Chebyshev distance
     *                   of the update center
     */
    public StreamingWorld(TerrainGenerator generator, int loadRadius) {
        this.generator = Objects.requireNonNull(generator, "generator");
        if (loadRadius < 0) {
            throw new IllegalArgumentException("loadRadius must be >= 0: " + loadRadius);
        }
        this.loadRadius = loadRadius;

        int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
        AtomicInteger threadNumber = new AtomicInteger();
        this.generationPool = Executors.newFixedThreadPool(threads, runnable -> {
            Thread thread = new Thread(runnable, "chunk-gen-" + threadNumber.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    /** Registers a listener for load/unload events; call before the first update. */
    public void addListener(ChunkListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * Advances streaming for one tick: publishes finished chunks, requests
     * missing ones around {@code center}, and unloads distant ones.
     */
    public void update(ChunkPos center) {
        publishCompleted();
        requestMissing(center);
        unloadDistant(center);
    }

    private void publishCompleted() {
        GeneratedChunk generated;
        while ((generated = completed.poll()) != null) {
            pending.remove(generated.position());
            chunks.put(generated.position(), generated.chunk());
            for (ChunkListener listener : listeners) {
                listener.onChunkLoaded(generated.position());
            }
        }
    }

    private void requestMissing(ChunkPos center) {
        for (int dx = -loadRadius; dx <= loadRadius; dx++) {
            for (int dz = -loadRadius; dz <= loadRadius; dz++) {
                ChunkPos position = new ChunkPos(center.x() + dx, center.z() + dz);
                if (chunks.containsKey(position) || !pending.add(position)) {
                    continue;
                }
                generationPool.submit(() -> {
                    Chunk chunk = new Chunk();
                    generator.generate(chunk, position);
                    completed.add(new GeneratedChunk(position, chunk));
                });
            }
        }
    }

    private void unloadDistant(ChunkPos center) {
        int unloadRadius = loadRadius + 1;
        Iterator<ChunkPos> iterator = chunks.keySet().iterator();
        while (iterator.hasNext()) {
            ChunkPos position = iterator.next();
            int distance = Math.max(Math.abs(position.x() - center.x()), Math.abs(position.z() - center.z()));
            if (distance > unloadRadius) {
                iterator.remove();
                for (ChunkListener listener : listeners) {
                    listener.onChunkUnloaded(position);
                }
            }
        }
    }

    @Override
    public BlockType block(int worldX, int worldY, int worldZ) {
        if (worldY < 0 || worldY >= Chunk.SIZE_Y) {
            return BlockType.AIR;
        }
        Chunk chunk = chunks.get(ChunkPos.containing(worldX, worldZ));
        if (chunk == null) {
            return BlockType.AIR;
        }
        return chunk.block(Math.floorMod(worldX, Chunk.SIZE_X), worldY, Math.floorMod(worldZ, Chunk.SIZE_Z));
    }

    @Override
    public boolean setBlock(int worldX, int worldY, int worldZ, BlockType type) {
        Objects.requireNonNull(type, "type");
        if (worldY < 0 || worldY >= Chunk.SIZE_Y) {
            return false;
        }
        Chunk chunk = chunks.get(ChunkPos.containing(worldX, worldZ));
        if (chunk == null) {
            return false;
        }
        int localX = Math.floorMod(worldX, Chunk.SIZE_X);
        int localZ = Math.floorMod(worldZ, Chunk.SIZE_Z);
        if (chunk.block(localX, worldY, localZ) == type) {
            return false;
        }
        chunk.setBlock(localX, worldY, localZ, type);
        return true;
    }

    @Override
    public Optional<Chunk> chunk(ChunkPos position) {
        return Optional.ofNullable(chunks.get(position));
    }

    @Override
    public Set<ChunkPos> chunkPositions() {
        return Set.copyOf(chunks.keySet());
    }

    @Override
    public void close() {
        generationPool.shutdownNow();
        try {
            if (!generationPool.awaitTermination(1, TimeUnit.SECONDS)) {
                System.err.println("Chunk generation pool did not terminate within 1s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
