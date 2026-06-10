package org.example.claudecraft.world;

import org.example.claudecraft.world.gen.TerrainGenerator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A world generated eagerly at construction: a square grid of chunks centered
 * on the origin. Placeholder for the streaming world that will load and unload
 * chunks around the player asynchronously.
 *
 * <p>Immutable after construction (until block editing lands); safe to read
 * from any thread.
 */
public final class GeneratedWorld implements World {

    private final Map<ChunkPos, Chunk> chunks = new LinkedHashMap<>();

    /**
     * Generates all chunks with {@code |x| <= radiusChunks} and
     * {@code |z| <= radiusChunks}.
     */
    public GeneratedWorld(TerrainGenerator generator, int radiusChunks) {
        Objects.requireNonNull(generator, "generator");
        if (radiusChunks < 0) {
            throw new IllegalArgumentException("radiusChunks must be >= 0: " + radiusChunks);
        }
        for (int chunkX = -radiusChunks; chunkX <= radiusChunks; chunkX++) {
            for (int chunkZ = -radiusChunks; chunkZ <= radiusChunks; chunkZ++) {
                ChunkPos position = new ChunkPos(chunkX, chunkZ);
                Chunk chunk = new Chunk();
                generator.generate(chunk, position);
                chunks.put(position, chunk);
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
    public Optional<Chunk> chunk(ChunkPos position) {
        return Optional.ofNullable(chunks.get(position));
    }

    @Override
    public Set<ChunkPos> chunkPositions() {
        return Set.copyOf(chunks.keySet());
    }
}
