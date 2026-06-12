package org.example.claudecraft.render;

import org.example.claudecraft.render.PlayerModelGeometry.PartGeometry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerModelGeometryTest {

    private static final int FLOATS_PER_VERTEX = 9;
    private static final float SCALE = 1.8f / 32.0f;

    /** The head box: 8×8×8 px at texture (0,0), centered on its pivot. */
    private static PartGeometry head() {
        return PlayerModelGeometry.box(0, 0, 8, 8, 8, -4, 0, -4, SCALE);
    }

    @Test
    void boxHasSixFacesOfFourVerticesAndTwelveTriangles() {
        PartGeometry geometry = head();
        assertEquals(24 * FLOATS_PER_VERTEX, geometry.vertices().length);
        assertEquals(36, geometry.indices().length);
        for (int index : geometry.indices()) {
            assertTrue(index >= 0 && index < 24, "index out of range: " + index);
        }
    }

    @Test
    void positionsSpanExactlyTheScaledBox() {
        float[] vertices = head().vertices();
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < vertices.length; i += FLOATS_PER_VERTEX) {
            min = Math.min(min, vertices[i]);
            max = Math.max(max, vertices[i]);
        }
        assertEquals(-4 * SCALE, min, 1e-6);
        assertEquals(4 * SCALE, max, 1e-6);
    }

    @Test
    void textureCoordinatesStayInsideTheHeadRegion() {
        // The whole head occupies (0,0)–(32,16) of the 64×32 skin.
        float[] vertices = head().vertices();
        for (int i = 0; i < vertices.length; i += FLOATS_PER_VERTEX) {
            float u = vertices[i + 3];
            float v = vertices[i + 4];
            assertTrue(u >= 0.0f && u <= 32.0f / 64.0f, "u out of region: " + u);
            assertTrue(v >= 0.0f && v <= 16.0f / 32.0f, "v out of region: " + v);
        }
    }

    @Test
    void windingIsCounterClockwiseAroundEachFaceNormal() {
        PartGeometry geometry = head();
        float[] vertices = geometry.vertices();
        int[] indices = geometry.indices();
        for (int triangle = 0; triangle < indices.length; triangle += 3) {
            int a = indices[triangle] * FLOATS_PER_VERTEX;
            int b = indices[triangle + 1] * FLOATS_PER_VERTEX;
            int c = indices[triangle + 2] * FLOATS_PER_VERTEX;
            // Cross product of the triangle edges must point along the stored normal.
            float e1x = vertices[b] - vertices[a];
            float e1y = vertices[b + 1] - vertices[a + 1];
            float e1z = vertices[b + 2] - vertices[a + 2];
            float e2x = vertices[c] - vertices[b];
            float e2y = vertices[c + 1] - vertices[b + 1];
            float e2z = vertices[c + 2] - vertices[b + 2];
            float crossX = e1y * e2z - e1z * e2y;
            float crossY = e1z * e2x - e1x * e2z;
            float crossZ = e1x * e2y - e1y * e2x;
            float dot = crossX * vertices[a + 5] + crossY * vertices[a + 6] + crossZ * vertices[a + 7];
            assertTrue(dot > 0.0f, "triangle at index " + triangle + " winds against its normal");
        }
    }

    @Test
    void everyVertexIsFullySkyLit() {
        float[] vertices = head().vertices();
        for (int i = 0; i < vertices.length; i += FLOATS_PER_VERTEX) {
            assertEquals(1.0f, vertices[i + 8]);
        }
    }
}
