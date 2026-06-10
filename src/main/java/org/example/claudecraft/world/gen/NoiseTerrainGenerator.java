package org.example.claudecraft.world.gen;

import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.Chunk;
import org.example.claudecraft.world.ChunkPos;
import org.joml.SimplexNoise;

import java.util.Random;

/**
 * Rolling-hills terrain from a fractal (fBm) simplex-noise heightmap: a few
 * octaves of {@link SimplexNoise} sampled in world space, so chunks line up
 * seamlessly. Each column gets grass on top, a few dirt layers, then stone.
 *
 * <p>Deterministic per seed: the seed picks fixed per-octave sample offsets.
 * Stateless after construction, safe for worker threads.
 */
public final class NoiseTerrainGenerator implements TerrainGenerator {

    private static final int OCTAVES = 4;
    private static final float BASE_FREQUENCY = 1.0f / 96.0f;
    private static final float LACUNARITY = 2.0f;
    private static final float PERSISTENCE = 0.5f;
    /** Keeps seed offsets small enough that frequency-scaled samples stay in float precision. */
    private static final float MAX_OFFSET = 100_000.0f;

    private static final int BASE_HEIGHT = 64;
    private static final int HEIGHT_AMPLITUDE = 20;
    private static final int DIRT_LAYERS = 3;

    private final float[] octaveOffsetX = new float[OCTAVES];
    private final float[] octaveOffsetZ = new float[OCTAVES];

    public NoiseTerrainGenerator(long seed) {
        Random random = new Random(seed);
        for (int octave = 0; octave < OCTAVES; octave++) {
            octaveOffsetX[octave] = random.nextFloat() * MAX_OFFSET;
            octaveOffsetZ[octave] = random.nextFloat() * MAX_OFFSET;
        }
    }

    @Override
    public void generate(Chunk chunk, ChunkPos position) {
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                int surface = surfaceHeight(position.minBlockX() + x, position.minBlockZ() + z);
                for (int y = 0; y <= surface; y++) {
                    chunk.setBlock(x, y, z, typeAt(y, surface));
                }
            }
        }
    }

    /** The y of the topmost solid block in the given world column. */
    private int surfaceHeight(int worldX, int worldZ) {
        float sum = 0.0f;
        float amplitude = 1.0f;
        float frequency = BASE_FREQUENCY;
        float amplitudeSum = 0.0f;
        for (int octave = 0; octave < OCTAVES; octave++) {
            sum += amplitude * SimplexNoise.noise(
                    (worldX + octaveOffsetX[octave]) * frequency,
                    (worldZ + octaveOffsetZ[octave]) * frequency);
            amplitudeSum += amplitude;
            amplitude *= PERSISTENCE;
            frequency *= LACUNARITY;
        }
        int height = BASE_HEIGHT + Math.round(sum / amplitudeSum * HEIGHT_AMPLITUDE);
        return Math.clamp(height, 1, Chunk.SIZE_Y - 1);
    }

    private BlockType typeAt(int y, int surface) {
        if (y == surface) {
            return BlockType.GRASS;
        }
        if (y >= surface - DIRT_LAYERS) {
            return BlockType.DIRT;
        }
        return BlockType.STONE;
    }
}
