package org.example.claudecraft.player;

import org.example.claudecraft.core.Input;
import org.example.claudecraft.physics.CollisionResolver;
import org.example.claudecraft.physics.MoveResult;
import org.example.claudecraft.world.ChunkPos;
import org.example.claudecraft.world.World;
import org.joml.Vector3f;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

/**
 * Survival-style movement: mouse look, WASD walking relative to the view yaw,
 * Space to jump, with gravity and AABB collision against the world. Physics
 * pauses (look still works) until the chunk under the player has streamed in,
 * so a fresh spawn cannot fall through ungenerated terrain.
 */
public final class PlayerController {

    private static final float MOUSE_SENSITIVITY = 0.0025f; // radians per pixel
    private static final float WALK_SPEED = 4.3f;           // blocks per second
    private static final float GRAVITY = 32.0f;             // blocks per second²
    private static final float JUMP_SPEED = 8.5f;           // ≈ 1.1 blocks high
    private static final float TERMINAL_FALL_SPEED = 78.0f;

    private final Player player;
    private final Input input;
    private final World world;
    /** Scratch vector reused every tick; update is a hot path and must not allocate. */
    private final Vector3f wishDirection = new Vector3f();

    public PlayerController(Player player, Input input, World world) {
        this.player = Objects.requireNonNull(player, "player");
        this.input = Objects.requireNonNull(input, "input");
        this.world = Objects.requireNonNull(world, "world");
    }

    /** Applies one simulation tick of look, movement and physics. */
    public void update(float dt) {
        player.turn(input.mouseDeltaX() * MOUSE_SENSITIVITY, input.mouseDeltaY() * MOUSE_SENSITIVITY);
        if (!chunkUnderPlayerLoaded()) {
            return;
        }

        Vector3f velocity = player.velocity();
        applyWalkInput(velocity);
        if (input.isKeyDown(GLFW_KEY_SPACE) && player.isOnGround()) {
            velocity.y = JUMP_SPEED;
        }
        velocity.y = Math.max(velocity.y - GRAVITY * dt, -TERMINAL_FALL_SPEED);

        MoveResult moved = CollisionResolver.move(
                world, player.boundingBox(), velocity.x * dt, velocity.y * dt, velocity.z * dt);
        player.translate(moved.movedX(), moved.movedY(), moved.movedZ());
        player.setOnGround(moved.onGround());
        if (moved.collidedY()) {
            velocity.y = 0.0f;
        }
    }

    private void applyWalkInput(Vector3f velocity) {
        float forward = axis(GLFW_KEY_W, GLFW_KEY_S);
        float strafe = axis(GLFW_KEY_D, GLFW_KEY_A);

        // Yaw 0 faces -Z: forward = (sin yaw, 0, -cos yaw), right = (cos yaw, 0, sin yaw).
        float sinYaw = org.joml.Math.sin(player.yaw());
        float cosYaw = org.joml.Math.cos(player.yaw());
        wishDirection.set(
                forward * sinYaw + strafe * cosYaw,
                0.0f,
                -forward * cosYaw + strafe * sinYaw);
        if (wishDirection.lengthSquared() > 1.0f) {
            wishDirection.normalize(); // diagonals are not faster
        }
        velocity.x = wishDirection.x * WALK_SPEED;
        velocity.z = wishDirection.z * WALK_SPEED;
    }

    private boolean chunkUnderPlayerLoaded() {
        ChunkPos position = ChunkPos.containing(
                (int) Math.floor(player.position().x()),
                (int) Math.floor(player.position().z()));
        return world.chunk(position).isPresent();
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
