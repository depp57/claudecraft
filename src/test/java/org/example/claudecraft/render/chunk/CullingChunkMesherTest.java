package org.example.claudecraft.render.chunk;

import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.Chunk;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CullingChunkMesherTest {

    private static final int FLOATS_PER_VERTEX = 9;
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
    void topFaceCarriesGrassTopUvsAndUpNormal() {
        Chunk chunk = new Chunk();
        chunk.setBlock(8, 100, 8, BlockType.GRASS);

        // Faces are emitted in Direction order, so the UP face is the first four vertices.
        float[] vertices = mesher.mesh(chunk).vertices();
        TextureTile grassTop = BlockTextures.tileFor(BlockType.GRASS, org.example.claudecraft.world.Direction.UP);

        for (int corner = 0; corner < VERTICES_PER_FACE; corner++) {
            int base = corner * FLOATS_PER_VERTEX;
            float u = vertices[base + 3];
            float v = vertices[base + 4];
            assertTrue(u >= grassTop.u0() && u <= grassTop.u1(), "u inside grass-top tile, got " + u);
            assertTrue(v >= grassTop.v0() && v <= grassTop.v1(), "v inside grass-top tile, got " + v);
            assertEquals(0.0f, vertices[base + 5], "normal x");
            assertEquals(1.0f, vertices[base + 6], "normal y points up");
            assertEquals(0.0f, vertices[base + 7], "normal z");
            assertEquals(1.0f, vertices[base + 8], "isolated block top sees the sky");
        }
    }

    @Test
    void faceUnderAnOverhangIsShadowed() {
        Chunk chunk = new Chunk();
        chunk.setBlock(8, 100, 8, BlockType.STONE);
        chunk.setBlock(8, 102, 8, BlockType.STONE); // overhang with an air gap at y = 101

        float[] vertices = mesher.mesh(chunk).vertices();

        // Blocks are visited bottom-up, faces in Direction order (UP first), so
        // the lower block's UP face is vertices 0–3 and the upper block's UP
        // face starts after the lower block's 6 faces, at vertex 24.
        int lowerTopSkyLight = 8;
        int upperTopSkyLight = 24 * FLOATS_PER_VERTEX + 8;
        assertEquals(0.0f, vertices[lowerTopSkyLight], "face under the overhang is covered");
        assertEquals(1.0f, vertices[upperTopSkyLight], "top of the overhang sees the sky");
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
