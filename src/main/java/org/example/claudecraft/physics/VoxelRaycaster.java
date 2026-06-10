package org.example.claudecraft.physics;

import org.example.claudecraft.world.Direction;
import org.example.claudecraft.world.World;
import org.joml.Vector3fc;

import java.util.Optional;

/**
 * Casts rays through the block grid using Amanatides &amp; Woo traversal:
 * visits every cell the ray passes through (no corner skipping) and returns
 * the first non-air block. Pure logic, safe on any thread.
 */
public final class VoxelRaycaster {

    private VoxelRaycaster() {
    }

    /**
     * Finds the first solid block along the ray.
     *
     * @param direction   need not be normalized, must be non-zero
     * @param maxDistance reach in blocks along the ray, must be positive
     * @return the hit, or empty if no solid block lies within reach. If the
     *         origin is already inside a solid block, that block is returned
     *         with face {@link Direction#UP}.
     */
    public static Optional<RayHit> raycast(World world, Vector3fc origin, Vector3fc direction, float maxDistance) {
        if (maxDistance <= 0.0f) {
            throw new IllegalArgumentException("maxDistance must be positive: " + maxDistance);
        }
        float length = direction.length();
        if (length == 0.0f || !Float.isFinite(length)) {
            throw new IllegalArgumentException("direction must be a non-zero finite vector");
        }
        float dirX = direction.x() / length;
        float dirY = direction.y() / length;
        float dirZ = direction.z() / length;

        int x = (int) Math.floor(origin.x());
        int y = (int) Math.floor(origin.y());
        int z = (int) Math.floor(origin.z());
        if (isSolid(world, x, y, z)) {
            return Optional.of(new RayHit(x, y, z, Direction.UP));
        }

        int stepX = (int) Math.signum(dirX);
        int stepY = (int) Math.signum(dirY);
        int stepZ = (int) Math.signum(dirZ);

        float tMaxX = boundaryDistance(origin.x(), x, dirX, stepX);
        float tMaxY = boundaryDistance(origin.y(), y, dirY, stepY);
        float tMaxZ = boundaryDistance(origin.z(), z, dirZ, stepZ);
        float tDeltaX = stepX == 0 ? Float.POSITIVE_INFINITY : Math.abs(1.0f / dirX);
        float tDeltaY = stepY == 0 ? Float.POSITIVE_INFINITY : Math.abs(1.0f / dirY);
        float tDeltaZ = stepZ == 0 ? Float.POSITIVE_INFINITY : Math.abs(1.0f / dirZ);

        while (true) {
            float t;
            Direction enteredFace;
            if (tMaxX <= tMaxY && tMaxX <= tMaxZ) {
                x += stepX;
                t = tMaxX;
                tMaxX += tDeltaX;
                enteredFace = stepX > 0 ? Direction.WEST : Direction.EAST;
            } else if (tMaxY <= tMaxZ) {
                y += stepY;
                t = tMaxY;
                tMaxY += tDeltaY;
                enteredFace = stepY > 0 ? Direction.DOWN : Direction.UP;
            } else {
                z += stepZ;
                t = tMaxZ;
                tMaxZ += tDeltaZ;
                enteredFace = stepZ > 0 ? Direction.NORTH : Direction.SOUTH;
            }
            if (t > maxDistance) {
                return Optional.empty();
            }
            if (isSolid(world, x, y, z)) {
                return Optional.of(new RayHit(x, y, z, enteredFace));
            }
        }
    }

    /** Ray parameter t at which the ray leaves the start cell along one axis. */
    private static float boundaryDistance(float originCoord, int cell, float dirComponent, int step) {
        if (step == 0) {
            return Float.POSITIVE_INFINITY;
        }
        float boundary = step > 0 ? cell + 1 : cell;
        return (boundary - originCoord) / dirComponent;
    }

    private static boolean isSolid(World world, int x, int y, int z) {
        return world.block(x, y, z).isSolid();
    }
}
