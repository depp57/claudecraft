package org.example.claudecraft.world;

import java.util.Objects;

/**
 * A 16×256×16 column of blocks, stored as a flat byte array of
 * {@link BlockType} ids. Coordinates are chunk-local: {@code x} and {@code z}
 * in {@code [0, 16)}, {@code y} in {@code [0, 256)}.
 *
 * <p>Mutable for performance; not thread-safe. A chunk being generated or
 * meshed on a worker thread must not be mutated concurrently.
 */
public final class Chunk {

    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 256;
    public static final int SIZE_Z = 16;

    private static final BlockType[] TYPES = BlockType.values();

    /** All AIR initially: BlockType.AIR.ordinal() == 0 matches the zeroed array. */
    private final byte[] blocks = new byte[SIZE_X * SIZE_Y * SIZE_Z];

    /** Returns true if the chunk-local coordinates lie inside chunk bounds. */
    public static boolean contains(int x, int y, int z) {
        return x >= 0 && x < SIZE_X && y >= 0 && y < SIZE_Y && z >= 0 && z < SIZE_Z;
    }

    /**
     * @throws IndexOutOfBoundsException if the coordinates are outside the chunk
     */
    public BlockType block(int x, int y, int z) {
        return TYPES[blocks[index(x, y, z)]];
    }

    /**
     * @throws IndexOutOfBoundsException if the coordinates are outside the chunk
     */
    public void setBlock(int x, int y, int z, BlockType type) {
        Objects.requireNonNull(type, "type");
        blocks[index(x, y, z)] = (byte) type.ordinal();
    }

    /**
     * An independent copy of this chunk's blocks, e.g. a snapshot handed to a
     * meshing worker so simulation edits cannot race the read.
     */
    public Chunk copy() {
        Chunk copy = new Chunk();
        System.arraycopy(blocks, 0, copy.blocks, 0, blocks.length);
        return copy;
    }

    private static int index(int x, int y, int z) {
        if (!contains(x, y, z)) {
            throw new IndexOutOfBoundsException(
                    "Block (" + x + ", " + y + ", " + z + ") outside chunk bounds "
                            + SIZE_X + "x" + SIZE_Y + "x" + SIZE_Z);
        }
        return (x * SIZE_Z + z) * SIZE_Y + y;
    }
}
