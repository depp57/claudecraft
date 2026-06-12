package org.example.claudecraft.render;

/**
 * Builds the boxes of the classic 64×32 humanoid skin layout as vertex data
 * in the shared {@link Mesh} layout (position, texCoord, normal, skyLight).
 *
 * <p>Coordinates are given in skin pixels relative to the part's pivot and
 * scaled to blocks; the model faces −Z. Pure math, no GL state.
 */
public final class PlayerModelGeometry {

    /** Interleaved 9-float vertices plus triangle indices for one box. */
    public record PartGeometry(float[] vertices, int[] indices) {
    }

    private static final float TEXTURE_WIDTH = 64.0f;
    private static final float TEXTURE_HEIGHT = 32.0f;
    private static final int FACES_PER_BOX = 6;
    private static final int FLOATS_PER_VERTEX = 9;
    private static final int FLOATS_PER_FACE = 4 * FLOATS_PER_VERTEX;

    private PlayerModelGeometry() {
    }

    /**
     * Builds one textured box in the standard skin layout: from texture
     * column {@code textureU}, the regions run right side, front, left side,
     * back, with top and bottom above them.
     *
     * @param textureU  left edge of the part's texture region, in pixels
     * @param textureV  top edge of the part's texture region, in pixels
     * @param pixelsWide   box width in pixels (x)
     * @param pixelsHigh   box height in pixels (y)
     * @param pixelsDeep   box depth in pixels (z)
     * @param minXPixels   minimum corner relative to the part pivot, in pixels
     * @param blocksPerPixel scale from skin pixels to world blocks
     */
    public static PartGeometry box(int textureU, int textureV,
                                   int pixelsWide, int pixelsHigh, int pixelsDeep,
                                   float minXPixels, float minYPixels, float minZPixels,
                                   float blocksPerPixel) {
        float x0 = minXPixels * blocksPerPixel;
        float y0 = minYPixels * blocksPerPixel;
        float z0 = minZPixels * blocksPerPixel;
        float x1 = (minXPixels + pixelsWide) * blocksPerPixel;
        float y1 = (minYPixels + pixelsHigh) * blocksPerPixel;
        float z1 = (minZPixels + pixelsDeep) * blocksPerPixel;

        float[] vertices = new float[FACES_PER_BOX * FLOATS_PER_FACE];
        int u = textureU;
        int v = textureV;
        int w = pixelsWide;
        int h = pixelsHigh;
        int d = pixelsDeep;

        // Front (−Z): texture-left is the character's right (+X).
        face(vertices, 0,
                x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0,
                0, 0, -1, u + d, v + d, w, h);
        // Back (+Z): texture-left is −X.
        face(vertices, 1,
                x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1,
                0, 0, 1, u + 2 * d + w, v + d, w, h);
        // Character's right side (+X): texture-left is the back (+Z).
        face(vertices, 2,
                x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1,
                1, 0, 0, u, v + d, d, h);
        // Character's left side (−X): texture-left is the front (−Z).
        face(vertices, 3,
                x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0,
                -1, 0, 0, u + d + w, v + d, d, h);
        // Top (+Y): texture bottom edge meets the front face's top edge.
        face(vertices, 4,
                x1, y1, z0, x0, y1, z0, x0, y1, z1, x1, y1, z1,
                0, 1, 0, u + d, v, w, d);
        // Bottom (−Y).
        face(vertices, 5,
                x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1,
                0, -1, 0, u + d + w, v, w, d);

        int[] indices = new int[FACES_PER_BOX * 6];
        for (int faceIndex = 0; faceIndex < FACES_PER_BOX; faceIndex++) {
            int vertexBase = faceIndex * 4;
            int indexBase = faceIndex * 6;
            indices[indexBase] = vertexBase;
            indices[indexBase + 1] = vertexBase + 1;
            indices[indexBase + 2] = vertexBase + 2;
            indices[indexBase + 3] = vertexBase;
            indices[indexBase + 4] = vertexBase + 2;
            indices[indexBase + 5] = vertexBase + 3;
        }
        return new PartGeometry(vertices, indices);
    }

    /**
     * Appends one quad whose corners are given counter-clockwise from outside,
     * ordered texture left-bottom, right-bottom, right-top, left-top. The UV
     * rectangle is in pixels with v measured from the top of the skin.
     */
    private static void face(float[] out, int faceIndex,
                             float ax, float ay, float az,
                             float bx, float by, float bz,
                             float cx, float cy, float cz,
                             float dx, float dy, float dz,
                             float nx, float ny, float nz,
                             int uLeftPx, int vTopPx, int uSizePx, int vSizePx) {
        float uLeft = uLeftPx / TEXTURE_WIDTH;
        float uRight = (uLeftPx + uSizePx) / TEXTURE_WIDTH;
        float vTop = vTopPx / TEXTURE_HEIGHT;
        float vBottom = (vTopPx + vSizePx) / TEXTURE_HEIGHT;
        int offset = faceIndex * FLOATS_PER_FACE;
        offset = vertex(out, offset, ax, ay, az, uLeft, vBottom, nx, ny, nz);
        offset = vertex(out, offset, bx, by, bz, uRight, vBottom, nx, ny, nz);
        offset = vertex(out, offset, cx, cy, cz, uRight, vTop, nx, ny, nz);
        vertex(out, offset, dx, dy, dz, uLeft, vTop, nx, ny, nz);
    }

    private static int vertex(float[] out, int offset,
                              float x, float y, float z, float u, float v,
                              float nx, float ny, float nz) {
        out[offset] = x;
        out[offset + 1] = y;
        out[offset + 2] = z;
        out[offset + 3] = u;
        out[offset + 4] = v;
        out[offset + 5] = nx;
        out[offset + 6] = ny;
        out[offset + 7] = nz;
        out[offset + 8] = 1.0f; // skyLight: the player is always fully sky-lit
        return offset + FLOATS_PER_VERTEX;
    }
}
