package org.example.claudecraft.physics;

import org.example.claudecraft.world.Direction;

import java.util.Objects;

/**
 * A solid block found by {@link VoxelRaycaster}: the block's position and the
 * face through which the ray entered it (its normal points back toward the
 * ray origin). A block placed against that face goes at
 * {@code (x + face.dx(), y + face.dy(), z + face.dz())}.
 */
public record RayHit(int x, int y, int z, Direction face) {

    public RayHit {
        Objects.requireNonNull(face, "face");
    }
}
