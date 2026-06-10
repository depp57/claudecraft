package org.example.claudecraft.physics;

import org.example.claudecraft.world.World;

/**
 * Moves an {@link AABB} through the voxel grid, clamping the movement against
 * solid blocks axis by axis: Y first (so an entity lands before sliding),
 * then X, then Z. This is the classic voxel-game resolution — it cannot
 * tunnel at survival-scale speeds and slides naturally along walls.
 *
 * <p>Pure logic, allocation-free per call except the result record; safe on
 * any thread that may read the world.
 */
public final class CollisionResolver {

    /** Gap kept between the box and a block it collided with, against float jitter. */
    private static final float EPSILON = 1.0e-4f;

    private CollisionResolver() {
    }

    /** Attempts to move the box by the given deltas, clamping against solid blocks. */
    public static MoveResult move(World world, AABB box, float dx, float dy, float dz) {
        float movedY = clampY(world, box, dy);
        box = box.offset(0.0f, movedY, 0.0f);
        float movedX = clampX(world, box, dx);
        box = box.offset(movedX, 0.0f, 0.0f);
        float movedZ = clampZ(world, box, dz);

        boolean collidedX = movedX != dx;
        boolean collidedY = movedY != dy;
        boolean collidedZ = movedZ != dz;
        boolean onGround = collidedY && dy < 0.0f;
        return new MoveResult(movedX, movedY, movedZ, collidedX, collidedY, collidedZ, onGround);
    }

    private static float clampY(World world, AABB box, float dy) {
        if (dy == 0.0f) {
            return 0.0f;
        }
        int x0 = floor(box.minX());
        int x1 = floor(box.maxX());
        int z0 = floor(box.minZ());
        int z1 = floor(box.maxZ());
        int y0 = floor(Math.min(box.minY(), box.minY() + dy));
        int y1 = floor(Math.max(box.maxY(), box.maxY() + dy));
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    if (world.block(x, y, z).isSolid()) {
                        dy = clipY(x, y, z, box, dy);
                    }
                }
            }
        }
        return dy;
    }

    private static float clampX(World world, AABB box, float dx) {
        if (dx == 0.0f) {
            return 0.0f;
        }
        int x0 = floor(Math.min(box.minX(), box.minX() + dx));
        int x1 = floor(Math.max(box.maxX(), box.maxX() + dx));
        int y0 = floor(box.minY());
        int y1 = floor(box.maxY());
        int z0 = floor(box.minZ());
        int z1 = floor(box.maxZ());
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    if (world.block(x, y, z).isSolid()) {
                        dx = clipX(x, y, z, box, dx);
                    }
                }
            }
        }
        return dx;
    }

    private static float clampZ(World world, AABB box, float dz) {
        if (dz == 0.0f) {
            return 0.0f;
        }
        int x0 = floor(box.minX());
        int x1 = floor(box.maxX());
        int y0 = floor(box.minY());
        int y1 = floor(box.maxY());
        int z0 = floor(Math.min(box.minZ(), box.minZ() + dz));
        int z1 = floor(Math.max(box.maxZ(), box.maxZ() + dz));
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    if (world.block(x, y, z).isSolid()) {
                        dz = clipZ(x, y, z, box, dz);
                    }
                }
            }
        }
        return dz;
    }

    private static float clipY(int blockX, int blockY, int blockZ, AABB box, float dy) {
        if (box.maxX() <= blockX || box.minX() >= blockX + 1
                || box.maxZ() <= blockZ || box.minZ() >= blockZ + 1) {
            return dy;
        }
        if (dy > 0.0f && box.maxY() <= blockY) {
            dy = Math.min(dy, blockY - box.maxY() - EPSILON);
        } else if (dy < 0.0f && box.minY() >= blockY + 1) {
            dy = Math.max(dy, blockY + 1 - box.minY() + EPSILON);
        }
        return dy;
    }

    private static float clipX(int blockX, int blockY, int blockZ, AABB box, float dx) {
        if (box.maxY() <= blockY || box.minY() >= blockY + 1
                || box.maxZ() <= blockZ || box.minZ() >= blockZ + 1) {
            return dx;
        }
        if (dx > 0.0f && box.maxX() <= blockX) {
            dx = Math.min(dx, blockX - box.maxX() - EPSILON);
        } else if (dx < 0.0f && box.minX() >= blockX + 1) {
            dx = Math.max(dx, blockX + 1 - box.minX() + EPSILON);
        }
        return dx;
    }

    private static float clipZ(int blockX, int blockY, int blockZ, AABB box, float dz) {
        if (box.maxX() <= blockX || box.minX() >= blockX + 1
                || box.maxY() <= blockY || box.minY() >= blockY + 1) {
            return dz;
        }
        if (dz > 0.0f && box.maxZ() <= blockZ) {
            dz = Math.min(dz, blockZ - box.maxZ() - EPSILON);
        } else if (dz < 0.0f && box.minZ() >= blockZ + 1) {
            dz = Math.max(dz, blockZ + 1 - box.minZ() + EPSILON);
        }
        return dz;
    }

    private static int floor(float value) {
        return (int) Math.floor(value);
    }
}
