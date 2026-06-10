package org.example.claudecraft.player;

import org.example.claudecraft.physics.AABB;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Objects;

/**
 * The player's physical state: feet position, velocity, ground contact and
 * view angles. The eye sits {@link #EYE_HEIGHT} above the feet.
 *
 * <p>Angles are in radians. Yaw 0 faces north (−Z) and increases turning
 * right; pitch 0 is level, positive looks down, clamped short of straight
 * up/down so the view never flips. Mutable; owned by the simulation tick.
 */
public final class Player {

    public static final float WIDTH = 0.6f;
    public static final float HEIGHT = 1.8f;
    public static final float EYE_HEIGHT = 1.62f;

    private static final float MAX_PITCH = (float) java.lang.Math.toRadians(89.0);

    private final Vector3f position;
    private final Vector3f velocity = new Vector3f();
    /** Scratch for {@link #eyePosition()}; recomputed on every call. */
    private final Vector3f eyePosition = new Vector3f();
    private boolean onGround;
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

    /** Teleports the player's feet to the given position. */
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    /** Read-only view of the feet position; valid until the next tick mutates it. */
    public Vector3fc position() {
        return position;
    }

    /**
     * The eye position (feet + {@link #EYE_HEIGHT}). Returns a reused scratch
     * vector — consume immediately, do not hold on to it.
     */
    public Vector3fc eyePosition() {
        return eyePosition.set(position).add(0.0f, EYE_HEIGHT, 0.0f);
    }

    /**
     * The mutable velocity in blocks per second, owned by the simulation
     * tick; the controller integrates and applies it.
     */
    public Vector3f velocity() {
        return velocity;
    }

    /** The player's collision box at the current position. */
    public AABB boundingBox() {
        return AABB.standingAt(position.x, position.y, position.z, WIDTH, HEIGHT);
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
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
