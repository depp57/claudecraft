package org.example.claudecraft.render.chunk;

/**
 * The UV rectangle of one tile inside the texture atlas. {@code (u0, v0)} is
 * the tile's top-left corner (images load top-down), {@code (u1, v1)} the
 * bottom-right.
 */
public record TextureTile(float u0, float v0, float u1, float v1) {

    /**
     * Computes the UV rectangle of the tile at grid position
     * {@code (tileX, tileY)} in a square atlas of {@code tilesPerRow}² tiles.
     */
    public static TextureTile of(int tileX, int tileY, int tilesPerRow) {
        if (tilesPerRow <= 0) {
            throw new IllegalArgumentException("tilesPerRow must be positive: " + tilesPerRow);
        }
        if (tileX < 0 || tileX >= tilesPerRow || tileY < 0 || tileY >= tilesPerRow) {
            throw new IllegalArgumentException(
                    "Tile (" + tileX + ", " + tileY + ") outside atlas grid of " + tilesPerRow + "x" + tilesPerRow);
        }
        float tileSize = 1.0f / tilesPerRow;
        return new TextureTile(
                tileX * tileSize, tileY * tileSize,
                (tileX + 1) * tileSize, (tileY + 1) * tileSize);
    }
}
