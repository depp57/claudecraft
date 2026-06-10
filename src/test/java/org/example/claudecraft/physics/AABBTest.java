package org.example.claudecraft.physics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AABBTest {

    private static final float EPSILON = 1e-6f;

    @Test
    void standingAtCentersHorizontallyAndStandsOnBase() {
        AABB box = AABB.standingAt(10.0f, 64.0f, -3.0f, 0.6f, 1.8f);
        assertEquals(9.7f, box.minX(), EPSILON);
        assertEquals(10.3f, box.maxX(), EPSILON);
        assertEquals(64.0f, box.minY(), EPSILON);
        assertEquals(65.8f, box.maxY(), EPSILON);
        assertEquals(-3.3f, box.minZ(), EPSILON);
        assertEquals(-2.7f, box.maxZ(), EPSILON);
    }

    @Test
    void intersectsRequiresOverlapNotTouch() {
        AABB box = new AABB(0, 0, 0, 1, 1, 1);
        assertTrue(box.intersects(new AABB(0.5f, 0.5f, 0.5f, 2, 2, 2)));
        assertFalse(box.intersects(new AABB(1, 0, 0, 2, 1, 1)), "shared face is touching, not intersecting");
        assertFalse(box.intersects(new AABB(3, 3, 3, 4, 4, 4)));
    }

    @Test
    void offsetTranslatesAllCorners() {
        AABB box = new AABB(0, 0, 0, 1, 2, 3).offset(1.0f, -2.0f, 0.5f);
        assertEquals(1.0f, box.minX(), EPSILON);
        assertEquals(-2.0f, box.minY(), EPSILON);
        assertEquals(0.0f, box.maxY(), EPSILON);
        assertEquals(3.5f, box.maxZ(), EPSILON);
    }

    @Test
    void rejectsInvertedBounds() {
        assertThrows(IllegalArgumentException.class, () -> new AABB(1, 0, 0, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> AABB.standingAt(0, 0, 0, -1.0f, 1.8f));
    }
}
