package org.example.claudecraft.render.chunk;

import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.Chunk;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CullingChunkMesherTest {

    private static final int FLOATS_PER_VERTEX = 6;
    private static final int VERTICES_PER_FACE = 4;
    private static final int INDICES_PER_FACE = 6;

    private final CullingChunkMesher mesher = new CullingChunkMesher();

    @Test
    void emptyChunkProducesNoGeometry() {
        MeshData data = mesher.mesh(new Chunk());
        assertTrue(data.isEmpty());
        assertEquals(0, data.vertices().length);
        assertEquals(0, data.indices().length);
    }

    @Test
    void isolatedBlockProducesSixFaces() {
        Chunk chunk = new Chunk();
        chunk.setBlock(8, 100, 8, BlockType.STONE);

        MeshData data = mesher.mesh(chunk);

        assertEquals(6 * VERTICES_PER_FACE * FLOATS_PER_VERTEX, data.vertices().length);
        assertEquals(6 * INDICES_PER_FACE, data.indices().length);
    }

    @Test
    void adjacentBlocksCullSharedFaces() {
        Chunk chunk = new Chunk();
        chunk.setBlock(8, 100, 8, BlockType.STONE);
        chunk.setBlock(8, 101, 8, BlockType.STONE);

        MeshData data = mesher.mesh(chunk);

        // Two cubes share one face pair: 12 faces minus 2 hidden = 10.
        assertEquals(10 * VERTICES_PER_FACE * FLOATS_PER_VERTEX, data.vertices().length);
        assertEquals(10 * INDICES_PER_FACE, data.indices().length);
    }

    @Test
    void blockOnChunkBorderKeepsOutwardFace() {
        Chunk chunk = new Chunk();
        chunk.setBlock(0, 0, 0, BlockType.STONE);

        MeshData data = mesher.mesh(chunk);

        // Out-of-chunk neighbors count as air, so all 6 faces are emitted.
        assertEquals(6 * INDICES_PER_FACE, data.indices().length);
    }

    @Test
    void isolatedBlockGeometrySpansExactlyOneCube() {
        Chunk chunk = new Chunk();
        chunk.setBlock(8, 100, 8, BlockType.STONE);

        float[] vertices = mesher.mesh(chunk).vertices();

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;
        for (int i = 0; i < vertices.length; i += FLOATS_PER_VERTEX) {
            minX = Math.min(minX, vertices[i]);
            maxX = Math.max(maxX, vertices[i]);
            minY = Math.min(minY, vertices[i + 1]);
            maxY = Math.max(maxY, vertices[i + 1]);
            minZ = Math.min(minZ, vertices[i + 2]);
            maxZ = Math.max(maxZ, vertices[i + 2]);
        }
        assertEquals(8.0f, minX);
        assertEquals(9.0f, maxX);
        assertEquals(100.0f, minY);
        assertEquals(101.0f, maxY);
        assertEquals(8.0f, minZ);
        assertEquals(9.0f, maxZ);
    }
}
