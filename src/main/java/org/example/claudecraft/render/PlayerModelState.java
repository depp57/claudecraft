package org.example.claudecraft.render;

/**
 * Snapshot of the player pose for drawing the third-person model: feet
 * position, view angles in radians, the walk-cycle state driving limb swing
 * ({@code swingAmplitude} 0 standing … 1 full stride), and the click swing
 * ({@code attackSwing} 0…1, 0 = at rest) raising the right arm.
 */
public record PlayerModelState(float x, float y, float z,
                               float yaw, float pitch,
                               float swingPhase, float swingAmplitude,
                               float attackSwing) {
}
