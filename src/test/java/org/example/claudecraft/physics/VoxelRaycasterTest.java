package org.example.claudecraft.physics;

import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.Direction;
import org.example.claudecraft.world.GeneratedWorld;
import org.example.claudecraft.world.World;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoxelRaycasterTest {

    /** An all-air 5x5-chunk world to place test blocks into. */
    private static World emptyWorld() {
        return new GeneratedWorld((chunk, position) -> {
        }, 2);
    }

    @Test
    void hitsNearestBlockAndReportsEnteredFace() {
        World world = emptyWorld();
        world.setBlock(8, 64, 5, BlockType.STONE);
        world.setBlock(8, 64, 7, BlockType.STONE);

        Optional<RayHit> hit = VoxelRaycaster.raycast(
                world, new Vector3f(8.5f, 64.5f, 0.5f), new Vector3f(0, 0, 1), 10.0f);

        assertEquals(Optional.of(new RayHit(8, 64, 5, Direction.NORTH)), hit);
    }

    @Test
    void reportsFacesForNegativeAndVerticalDirections() {
        World world = emptyWorld();
        world.setBlock(5, 64, 8, BlockType.STONE);
        world.setBlock(8, 60, 8, BlockType.STONE);

        Optional<RayHit> westward = VoxelRaycaster.raycast(
                world, new Vector3f(8.5f, 64.5f, 8.5f), new Vector3f(-1, 0, 0), 10.0f);
        Optional<RayHit> downward = VoxelRaycaster.raycast(
                world, new Vector3f(8.5f, 64.5f, 8.5f), new Vector3f(0, -1, 0), 10.0f);

        assertEquals(Optional.of(new RayHit(5, 64, 8, Direction.EAST)), westward);
        assertEquals(Optional.of(new RayHit(8, 60, 8, Direction.UP)), downward);
    }

    @Test
    void respectsMaxDistance() {
        World world = emptyWorld();
        world.setBlock(8, 64, 10, BlockType.STONE);
        Vector3f origin = new Vector3f(8.5f, 64.5f, 0.5f);
        Vector3f forward = new Vector3f(0, 0, 1);

        assertTrue(VoxelRaycaster.raycast(world, origin, forward, 6.0f).isEmpty());
        assertTrue(VoxelRaycaster.raycast(world, origin, forward, 12.0f).isPresent());
    }

    @Test
    void directionNeedNotBeNormalized() {
        World world = emptyWorld();
        world.setBlock(8, 64, 5, BlockType.STONE);

        Optional<RayHit> hit = VoxelRaycaster.raycast(
                world, new Vector3f(8.5f, 64.5f, 0.5f), new Vector3f(0, 0, 100), 10.0f);

        assertEquals(Optional.of(new RayHit(8, 64, 5, Direction.NORTH)), hit);
    }

    @Test
    void diagonalRayDoesNotSkipCells() {
        World world = emptyWorld();
        // Wall across the diagonal path; a naive sampler could tunnel through.
        for (int x = 0; x < 16; x++) {
            world.setBlock(x, 64, 8, BlockType.STONE);
        }

        Optional<RayHit> hit = VoxelRaycaster.raycast(
                world, new Vector3f(4.5f, 64.5f, 4.5f), new Vector3f(1, 0, 1), 12.0f);

        assertTrue(hit.isPresent());
        assertEquals(8, hit.get().z());
        assertEquals(Direction.NORTH, hit.get().face());
    }

    @Test
    void originInsideSolidBlockHitsImmediately() {
        World world = emptyWorld();
        world.setBlock(8, 64, 8, BlockType.STONE);

        Optional<RayHit> hit = VoxelRaycaster.raycast(
                world, new Vector3f(8.5f, 64.5f, 8.5f), new Vector3f(0, 0, 1), 10.0f);

        assertEquals(Optional.of(new RayHit(8, 64, 8, Direction.UP)), hit);
    }

    @Test
    void rejectsInvalidArguments() {
        World world = emptyWorld();
        Vector3f origin = new Vector3f();

        assertThrows(IllegalArgumentException.class,
                () -> VoxelRaycaster.raycast(world, origin, new Vector3f(0, 0, 0), 10.0f));
        assertThrows(IllegalArgumentException.class,
                () -> VoxelRaycaster.raycast(world, origin, new Vector3f(0, 0, 1), 0.0f));
    }
}
