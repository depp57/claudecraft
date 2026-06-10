package org.example.claudecraft.player;

import org.example.claudecraft.core.Input;
import org.joml.Vector3f;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

/**
 * Creative-flight movement: mouse look, WASD relative to the view yaw on the
 * horizontal plane, Space/Left-Shift to fly up/down. Gravity and collision
 * arrive with the physics step.
 */
public final class PlayerController {

    private static final float MOUSE_SENSITIVITY = 0.0025f; // radians per pixel
    private static final float FLY_SPEED = 12.0f;           // blocks per second

    private final Player player;
    private final Input input;
    /** Scratch vector reused every tick; update is a hot path and must not allocate. */
    private final Vector3f wishDirection = new Vector3f();

    public PlayerController(Player player, Input input) {
        this.player = Objects.requireNonNull(player, "player");
        this.input = Objects.requireNonNull(input, "input");
    }

    /** Applies one simulation tick of look and movement input. */
    public void update(float dt) {
        player.turn(input.mouseDeltaX() * MOUSE_SENSITIVITY, input.mouseDeltaY() * MOUSE_SENSITIVITY);

        float forward = axis(GLFW_KEY_W, GLFW_KEY_S);
        float strafe = axis(GLFW_KEY_D, GLFW_KEY_A);
        float vertical = axis(GLFW_KEY_SPACE, GLFW_KEY_LEFT_SHIFT);
        if (forward == 0.0f && strafe == 0.0f && vertical == 0.0f) {
            return;
        }

        // Yaw 0 faces -Z: forward = (sin yaw, 0, -cos yaw), right = (cos yaw, 0, sin yaw).
        float sinYaw = org.joml.Math.sin(player.yaw());
        float cosYaw = org.joml.Math.cos(player.yaw());
        wishDirection.set(
                forward * sinYaw + strafe * cosYaw,
                vertical,
                -forward * cosYaw + strafe * sinYaw);
        if (wishDirection.lengthSquared() > 1.0f) {
            wishDirection.normalize(); // diagonals are not faster
        }
        float distance = FLY_SPEED * dt;
        player.translate(wishDirection.x * distance, wishDirection.y * distance, wishDirection.z * distance);
    }

    private float axis(int positiveKey, int negativeKey) {
        float value = 0.0f;
        if (input.isKeyDown(positiveKey)) {
            value += 1.0f;
        }
        if (input.isKeyDown(negativeKey)) {
            value -= 1.0f;
        }
        return value;
    }
}
