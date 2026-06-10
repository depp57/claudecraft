package org.example.claudecraft.render;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * First-person camera: turns an eye pose (position + yaw/pitch, matching the
 * {@code Player} angle conventions) into a perspective view-projection matrix.
 *
 * <p>Pure math, no GL state. The returned matrix is reused across frames —
 * consume it immediately, do not hold on to it.
 */
public final class Camera {

    private static final float FOV_RADIANS = (float) Math.toRadians(70.0);
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 1000.0f;

    private final Vector3f position = new Vector3f();
    private float yaw;
    private float pitch;
    private final Matrix4f viewProjection = new Matrix4f();
    private final Matrix4f skyInverse = new Matrix4f();

    /** Updates the camera pose; angles in radians. */
    public void setPose(Vector3fc position, float yaw, float pitch) {
        this.position.set(position);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /** Read-only view of the eye position; valid until the next {@link #setPose}. */
    public Vector3fc position() {
        return position;
    }

    /**
     * Inverse of projection × rotation-only view, for turning NDC coordinates
     * back into view rays (skybox). The returned matrix is reused across
     * frames — consume it immediately.
     */
    public Matrix4fc skyViewProjectionInverse(float aspectRatio) {
        return skyInverse.setPerspective(FOV_RADIANS, aspectRatio, NEAR_PLANE, FAR_PLANE)
                .rotateX(pitch)
                .rotateY(yaw)
                .invert();
    }

    /**
     * Recomputes and returns the view-projection matrix for the current pose.
     *
     * @param aspectRatio framebuffer width / height, must be positive
     */
    public Matrix4fc viewProjection(float aspectRatio) {
        if (aspectRatio <= 0.0f) {
            throw new IllegalArgumentException("aspectRatio must be positive: " + aspectRatio);
        }
        return viewProjection.setPerspective(FOV_RADIANS, aspectRatio, NEAR_PLANE, FAR_PLANE)
                .rotateX(pitch)
                .rotateY(yaw)
                .translate(-position.x, -position.y, -position.z);
    }
}
