package org.example.claudecraft.player;

import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Objects;

/**
 * The player's pose in the world: eye position plus view angles.
 *
 * <p>Angles are in radians. Yaw 0 faces north (−Z) and increases turning
 * right; pitch 0 is level, positive looks down, clamped short of straight
 * up/down so the view never flips. Mutable; owned by the simulation tick.
 */
public final class Player {

    private static final float MAX_PITCH = (float) java.lang.Math.toRadians(89.0);

    private final Vector3f position;
    private float yaw;
    private float pitch;

    public Player(Vector3fc spawnPosition) {
        Objects.requireNonNull(spawnPosition, "spawnPosition");
        this.position = new Vector3f(spawnPosition);
    }

    /** Rotates the view, clamping pitch to ±89°. */
    public void turn(float deltaYaw, float deltaPitch) {
        yaw += deltaYaw;
        pitch = Math.clamp(-MAX_PITCH, MAX_PITCH, pitch + deltaPitch);
    }

    /** Moves the player by the given world-space offset. */
    public void translate(float dx, float dy, float dz) {
        position.add(dx, dy, dz);
    }

    /** Read-only view of the eye position; valid until the next tick mutates it. */
    public Vector3fc position() {
        return position;
    }

    /**
     * Writes the unit view direction for the current yaw/pitch into
     * {@code dest} and returns it.
     */
    public Vector3f lookDirection(Vector3f dest) {
        float cosPitch = Math.cos(pitch);
        return dest.set(
                Math.sin(yaw) * cosPitch,
                -Math.sin(pitch),
                -Math.cos(yaw) * cosPitch);
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }
}
