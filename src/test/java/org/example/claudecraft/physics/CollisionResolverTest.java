package org.example.claudecraft.physics;

import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.GeneratedWorld;
import org.example.claudecraft.world.World;
import org.example.claudecraft.world.gen.FlatTerrainGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollisionResolverTest {

    /** Flat world: topmost solid block at y = 64, walkable surface at y = 65. */
    private static final int GROUND = 64;
    private static final float SURFACE = GROUND + 1;
    private static final float TOLERANCE = 1e-3f;

    private static World flatWorld() {
        return new GeneratedWorld(new FlatTerrainGenerator(GROUND), 1);
    }

    private static AABB playerBoxAt(float x, float baseY, float z) {
        return AABB.standingAt(x, baseY, z, 0.6f, 1.8f);
    }

    @Test
    void freeMovementInAirIsUnclamped() {
        MoveResult moved = CollisionResolver.move(
                flatWorld(), playerBoxAt(8.5f, SURFACE + 5, 8.5f), 0.5f, 0.25f, -0.5f);

        assertEquals(0.5f, moved.movedX());
        assertEquals(0.25f, moved.movedY());
        assertEquals(-0.5f, moved.movedZ());
        assertFalse(moved.collidedX() || moved.collidedY() || moved.collidedZ());
        assertFalse(moved.onGround());
    }

    @Test
    void fallingIsClampedAtTheSurfaceAndSetsOnGround() {
        MoveResult moved = CollisionResolver.move(
                flatWorld(), playerBoxAt(8.5f, SURFACE + 5, 8.5f), 0.0f, -10.0f, 0.0f);

        assertEquals(-5.0f, moved.movedY(), TOLERANCE);
        assertTrue(moved.collidedY());
        assertTrue(moved.onGround());
    }

    @Test
    void walkingIntoWallClampsOnlyThatAxis() {
        World world = flatWorld();
        world.setBlock(12, GROUND + 1, 8, BlockType.STONE);
        world.setBlock(12, GROUND + 2, 8, BlockType.STONE);

        // Box at x ∈ [10.2, 10.8] moving +x toward the wall face at x = 12, sliding in z.
        MoveResult moved = CollisionResolver.move(
                world, playerBoxAt(10.5f, SURFACE, 8.5f), 3.0f, 0.0f, 0.3f);

        assertEquals(1.2f, moved.movedX(), TOLERANCE);
        assertTrue(moved.collidedX());
        assertEquals(0.3f, moved.movedZ(), TOLERANCE);
        assertFalse(moved.collidedZ());
        assertFalse(moved.onGround(), "no vertical movement was attempted");
    }

    @Test
    void jumpIsClampedByCeiling() {
        World world = flatWorld();
        world.setBlock(8, GROUND + 4, 8, BlockType.STONE); // ceiling at y = 68, box top at 66.8

        MoveResult moved = CollisionResolver.move(
                world, playerBoxAt(8.5f, SURFACE, 8.5f), 0.0f, 2.0f, 0.0f);

        assertEquals(1.2f, moved.movedY(), TOLERANCE);
        assertTrue(moved.collidedY());
        assertFalse(moved.onGround(), "hitting a ceiling is not landing");
    }

    @Test
    void restingOnTheSurfaceDoesNotSinkOrStick() {
        World world = flatWorld();
        AABB resting = playerBoxAt(8.5f, SURFACE + 1e-4f, 8.5f);

        // Gravity keeps pulling every tick; movement must clamp to ~zero.
        MoveResult gravityTick = CollisionResolver.move(world, resting, 0.0f, -0.5f, 0.0f);
        assertEquals(0.0f, gravityTick.movedY(), TOLERANCE);
        assertTrue(gravityTick.onGround());

        // And horizontal walking on the surface stays free.
        MoveResult walkTick = CollisionResolver.move(world, resting, 0.1f, -0.5f, 0.1f);
        assertEquals(0.1f, walkTick.movedX(), TOLERANCE);
        assertEquals(0.1f, walkTick.movedZ(), TOLERANCE);
        assertFalse(walkTick.collidedX() || walkTick.collidedZ());
    }
}
