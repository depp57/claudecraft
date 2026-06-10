package org.example.claudecraft.physics;

/**
 * Outcome of one {@link CollisionResolver#move} call: the movement actually
 * applied after clamping against solid blocks, which axes were clamped, and
 * whether the box ended up standing on ground (clamped while moving down).
 */
public record MoveResult(
        float movedX, float movedY, float movedZ,
        boolean collidedX, boolean collidedY, boolean collidedZ,
        boolean onGround) {
}
