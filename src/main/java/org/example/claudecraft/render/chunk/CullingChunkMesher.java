package org.example.claudecraft.render.chunk;

import org.example.claudecraft.util.FloatList;
import org.example.claudecraft.util.IntList;
import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.Chunk;
import org.example.claudecraft.world.Direction;

/**
 * Meshes a chunk by emitting one quad per visible block face: faces whose
 * neighbor is opaque are culled. Neighbors outside the chunk count as air
 * until cross-chunk lookups arrive with the multi-chunk world.
 *
 * <p>Quads are wound counter-clockwise seen from outside the block, so
 * {@code GL_CULL_FACE} with the default CCW front face works. Until the
 * texture atlas lands, blocks get placeholder colors, darkened per face
 * direction for depth perception.
 */
public final class CullingChunkMesher implements ChunkMesher {

    private static final int FLOATS_PER_VERTEX = 6;
    private static final int VERTICES_PER_FACE = 4;

    // Corner offsets (4 corners × xyz) per face, CCW from outside the block.
    private static final float[] UP_CORNERS = {0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0};
    private static final float[] DOWN_CORNERS = {0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1};
    private static final float[] NORTH_CORNERS = {0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0};
    private static final float[] SOUTH_CORNERS = {0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1};
    private static final float[] WEST_CORNERS = {0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0};
    private static final float[] EAST_CORNERS = {1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1};

    private static final float[] GRASS_COLOR = {0.35f, 0.65f, 0.25f};
    private static final float[] DIRT_COLOR = {0.45f, 0.32f, 0.22f};
    private static final float[] STONE_COLOR = {0.55f, 0.55f, 0.55f};

    @Override
    public MeshData mesh(Chunk chunk) {
        FloatList vertices = new FloatList(4096);
        IntList indices = new IntList(1024);
        int vertexCount = 0;

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                for (int y = 0; y < Chunk.SIZE_Y; y++) {
                    BlockType block = chunk.block(x, y, z);
                    if (!block.isOpaque()) {
                        continue;
                    }
                    for (Direction direction : Direction.values()) {
                        if (isFaceHidden(chunk, x, y, z, direction)) {
                            continue;
                        }
                        emitFace(vertices, x, y, z, direction, block);
                        indices.add(vertexCount);
                        indices.add(vertexCount + 1);
                        indices.add(vertexCount + 2);
                        indices.add(vertexCount + 2);
                        indices.add(vertexCount + 3);
                        indices.add(vertexCount);
                        vertexCount += VERTICES_PER_FACE;
                    }
                }
            }
        }
        return new MeshData(vertices.toArray(), indices.toArray());
    }

    private static boolean isFaceHidden(Chunk chunk, int x, int y, int z, Direction direction) {
        int nx = x + direction.dx();
        int ny = y + direction.dy();
        int nz = z + direction.dz();
        return Chunk.contains(nx, ny, nz) && chunk.block(nx, ny, nz).isOpaque();
    }

    private static void emitFace(FloatList vertices, int x, int y, int z, Direction direction, BlockType block) {
        float[] corners = cornersOf(direction);
        float[] color = colorOf(block);
        float brightness = brightnessOf(direction);

        for (int corner = 0; corner < VERTICES_PER_FACE; corner++) {
            int base = corner * 3;
            vertices.add(x + corners[base]);
            vertices.add(y + corners[base + 1]);
            vertices.add(z + corners[base + 2]);
            vertices.add(color[0] * brightness);
            vertices.add(color[1] * brightness);
            vertices.add(color[2] * brightness);
        }
    }

    private static float[] cornersOf(Direction direction) {
        return switch (direction) {
            case UP -> UP_CORNERS;
            case DOWN -> DOWN_CORNERS;
            case NORTH -> NORTH_CORNERS;
            case SOUTH -> SOUTH_CORNERS;
            case WEST -> WEST_CORNERS;
            case EAST -> EAST_CORNERS;
        };
    }

    private static float brightnessOf(Direction direction) {
        return switch (direction) {
            case UP -> 1.0f;
            case DOWN -> 0.5f;
            case NORTH, SOUTH -> 0.8f;
            case WEST, EAST -> 0.65f;
        };
    }

    private static float[] colorOf(BlockType block) {
        return switch (block) {
            case GRASS -> GRASS_COLOR;
            case DIRT -> DIRT_COLOR;
            case STONE -> STONE_COLOR;
            case AIR -> throw new IllegalArgumentException("AIR has no faces to color");
        };
    }
}
