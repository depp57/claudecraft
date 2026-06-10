package org.example.claudecraft.physics;

/**
 * An immutable axis-aligned bounding box. Boxes that merely touch (shared
 * face) do not count as intersecting.
 */
public record AABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {

    public AABB {
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException(
                    "min must not exceed max: (" + minX + ", " + minY + ", " + minZ
                            + ") to (" + maxX + ", " + maxY + ", " + maxZ + ")");
        }
    }

    /**
     * A box standing on {@code baseY}, centered horizontally on
     * {@code (centerX, centerZ)} — the natural shape for an entity standing
     * at a feet position.
     */
    public static AABB standingAt(float centerX, float baseY, float centerZ, float width, float height) {
        if (width < 0.0f || height < 0.0f) {
            throw new IllegalArgumentException("width and height must be >= 0: " + width + ", " + height);
        }
        float halfWidth = width / 2.0f;
        return new AABB(
                centerX - halfWidth, baseY, centerZ - halfWidth,
                centerX + halfWidth, baseY + height, centerZ + halfWidth);
    }

    /** The unit cube of the block cell at the given coordinates. */
    public static AABB blockAt(int x, int y, int z) {
        return new AABB(x, y, z, x + 1.0f, y + 1.0f, z + 1.0f);
    }

    public AABB offset(float dx, float dy, float dz) {
        return new AABB(minX + dx, minY + dy, minZ + dz, maxX + dx, maxY + dy, maxZ + dz);
    }

    public boolean intersects(AABB other) {
        return minX < other.maxX && maxX > other.minX
                && minY < other.maxY && maxY > other.minY
                && minZ < other.maxZ && maxZ > other.minZ;
    }
}
