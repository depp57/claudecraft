package org.example.claudecraft.render.chunk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextureTileTest {

    private static final float EPSILON = 1e-6f;

    @Test
    void firstTileSpansTopLeftQuarterCell() {
        TextureTile tile = TextureTile.of(0, 0, 4);
        assertEquals(0.0f, tile.u0(), EPSILON);
        assertEquals(0.0f, tile.v0(), EPSILON);
        assertEquals(0.25f, tile.u1(), EPSILON);
        assertEquals(0.25f, tile.v1(), EPSILON);
    }

    @Test
    void lastTileEndsAtOne() {
        TextureTile tile = TextureTile.of(3, 3, 4);
        assertEquals(0.75f, tile.u0(), EPSILON);
        assertEquals(0.75f, tile.v0(), EPSILON);
        assertEquals(1.0f, tile.u1(), EPSILON);
        assertEquals(1.0f, tile.v1(), EPSILON);
    }

    @Test
    void rejectsTilesOutsideGrid() {
        assertThrows(IllegalArgumentException.class, () -> TextureTile.of(-1, 0, 4));
        assertThrows(IllegalArgumentException.class, () -> TextureTile.of(4, 0, 4));
        assertThrows(IllegalArgumentException.class, () -> TextureTile.of(0, 4, 4));
        assertThrows(IllegalArgumentException.class, () -> TextureTile.of(0, 0, 0));
    }
}
