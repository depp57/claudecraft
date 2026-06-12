package org.example.claudecraft;

import org.example.claudecraft.audio.AudioException;
import org.example.claudecraft.audio.OpenAlSoundSystem;
import org.example.claudecraft.audio.SoundSystem;
import org.example.claudecraft.core.Game;
import org.example.claudecraft.core.Input;
import org.example.claudecraft.core.Window;
import org.example.claudecraft.physics.RayHit;
import org.example.claudecraft.physics.VoxelRaycaster;
import org.example.claudecraft.player.BlockInteraction;
import org.example.claudecraft.player.Player;
import org.example.claudecraft.player.PlayerController;
import org.example.claudecraft.render.PlayerModelState;
import org.example.claudecraft.render.Renderer;
import org.example.claudecraft.render.ViewPose;
import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.ChunkPos;
import org.example.claudecraft.world.DayNightCycle;
import org.example.claudecraft.world.StreamingWorld;
import org.example.claudecraft.world.gen.NoiseTerrainGenerator;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Objects;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F5;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

/**
 * Root game object wiring world, input, simulation and rendering together.
 * Chunks stream in around the player, who walks and jumps over the terrain
 * (WASD + mouse look, Space to jump), breaking blocks with left click and
 * placing stone with right click; ESC exits.
 */
public final class ClaudecraftGame implements Game, AutoCloseable {

    private static final long WORLD_SEED = 20260610L;
    private static final int LOAD_RADIUS_CHUNKS = 8;
    /** Above the tallest terrain (64 ± 20); the player falls onto the ground from here. */
    private static final Vector3f SPAWN = new Vector3f(8.5f, 90.0f, 8.5f);
    /** Falling below this means the player slipped out of the world; respawn instead. */
    private static final float VOID_RESET_Y = -32.0f;
    private static final float DAY_LENGTH_SECONDS = 120.0f;
    /** Start mid-morning so the world is well-lit on launch. */
    private static final float DAY_START_FRACTION = 0.1f;
    /** Horizontal distance walked between footstep sounds. */
    private static final float STEP_DISTANCE_BLOCKS = 2.2f;
    /** Slower than this is drifting, not walking — no footsteps. */
    private static final float STEP_MIN_SPEED = 0.5f;
    /** One full limb-swing cycle is two steps, keeping swing and step sounds in sync. */
    private static final float SWING_RADIANS_PER_BLOCK = (float) (Math.PI / STEP_DISTANCE_BLOCKS);
    /** Walking speed at which limbs reach full swing; matches the controller's walk speed. */
    private static final float FULL_SWING_SPEED = 4.3f;
    /** How quickly limb swing fades in/out when starting/stopping, per second. */
    private static final float SWING_FADE_RATE = 8.0f;
    /** Duration of one arm swing when clicking, in seconds. */
    private static final float ATTACK_SWING_SECONDS = 0.3f;
    private static final float THIRD_PERSON_DISTANCE = 4.0f;
    /** Kept between the camera and any wall behind the player. */
    private static final float CAMERA_COLLISION_MARGIN = 0.2f;
    private static final float MIN_CAMERA_DISTANCE = 0.3f;

    private final Window window;
    private final Input input;
    private final StreamingWorld world;
    private final Player player;
    private final PlayerController controller;
    private final Renderer renderer;
    private final BlockInteraction interaction;
    private final SoundSystem audio;
    private final DayNightCycle dayNightCycle = new DayNightCycle(DAY_LENGTH_SECONDS, DAY_START_FRACTION);
    private float distanceSinceStep;
    private boolean thirdPerson;
    private float swingPhase;
    private float swingAmplitude;
    /** 0 = arm at rest; runs to 1 over {@link #ATTACK_SWING_SECONDS} after a click. */
    private float attackSwing;
    private boolean attackSwinging;
    /** Scratch vectors reused every frame; render is a hot path and must not allocate. */
    private final Vector3f cameraDirection = new Vector3f();
    private final Vector3f cameraPosition = new Vector3f();

    public ClaudecraftGame(Window window) {
        this.window = Objects.requireNonNull(window, "window");
        this.input = new Input(window);
        this.world = new StreamingWorld(new NoiseTerrainGenerator(WORLD_SEED), LOAD_RADIUS_CHUNKS);
        this.player = new Player(SPAWN);
        this.audio = createSoundSystem();
        this.controller = new PlayerController(player, input, world,
                () -> audio.playJump(blockUnderPlayer()));
        this.renderer = new Renderer(world);
        this.interaction = new BlockInteraction(world, player, input, renderer::onBlockChanged,
                audio::playDig);
        world.addListener(renderer);
    }

    /** The game stays playable without sound if no audio device is available. */
    private static SoundSystem createSoundSystem() {
        try {
            return new OpenAlSoundSystem();
        } catch (AudioException e) {
            System.err.println("Audio disabled: " + e.getMessage());
            return SoundSystem.silent();
        }
    }

    @Override
    public void update(float dt) {
        input.beginTick();
        if (input.isKeyDown(GLFW_KEY_ESCAPE)) {
            window.requestClose();
        }
        if (input.isKeyJustPressed(GLFW_KEY_F5)) {
            thirdPerson = !thirdPerson;
        }
        controller.update(dt);
        interaction.update();
        world.update(playerChunk());
        dayNightCycle.update(dt);

        Vector3f velocity = player.velocity();
        float horizontalSpeed = (float) Math.hypot(velocity.x, velocity.z);
        updateFootsteps(dt, horizontalSpeed);
        updateLimbSwing(dt, horizontalSpeed);
        updateAttackSwing(dt);
        audio.update();

        if (player.position().y() < VOID_RESET_Y) {
            player.setPosition(SPAWN.x, SPAWN.y, SPAWN.z);
            player.velocity().zero();
        }
    }

    /** Plays a footstep every {@link #STEP_DISTANCE_BLOCKS} walked on the ground. */
    private void updateFootsteps(float dt, float horizontalSpeed) {
        if (!player.isOnGround() || horizontalSpeed < STEP_MIN_SPEED) {
            return;
        }
        distanceSinceStep += horizontalSpeed * dt;
        if (distanceSinceStep >= STEP_DISTANCE_BLOCKS) {
            distanceSinceStep = 0.0f;
            audio.playStep(blockUnderPlayer());
        }
    }

    /** Starts the arm swing on any click (even one that hits nothing) and runs it to completion. */
    private void updateAttackSwing(float dt) {
        if (input.isButtonJustPressed(GLFW_MOUSE_BUTTON_LEFT)
                || input.isButtonJustPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
            attackSwinging = true;
            attackSwing = 0.0f;
        }
        if (attackSwinging) {
            attackSwing += dt / ATTACK_SWING_SECONDS;
            if (attackSwing >= 1.0f) {
                attackSwinging = false;
                attackSwing = 0.0f;
            }
        }
    }

    /** Advances the third-person walk cycle and eases its amplitude in and out. */
    private void updateLimbSwing(float dt, float horizontalSpeed) {
        swingPhase += horizontalSpeed * dt * SWING_RADIANS_PER_BLOCK;
        float target = Math.min(1.0f, horizontalSpeed / FULL_SWING_SPEED);
        float blend = Math.min(1.0f, SWING_FADE_RATE * dt);
        swingAmplitude += (target - swingAmplitude) * blend;
    }

    /** The block directly below the player's feet (AIR when standing on an edge). */
    private BlockType blockUnderPlayer() {
        Vector3fc feet = player.position();
        return world.block(
                (int) Math.floor(feet.x()),
                (int) Math.floor(feet.y() - 0.5f),
                (int) Math.floor(feet.z()));
    }

    private ChunkPos playerChunk() {
        return ChunkPos.containing(
                (int) Math.floor(player.position().x()),
                (int) Math.floor(player.position().z()));
    }

    @Override
    public void render(float alpha) {
        int width = window.framebufferWidth();
        int height = window.framebufferHeight();
        if (width == 0 || height == 0) {
            return; // minimized
        }

        ViewPose viewPose;
        if (thirdPerson) {
            placeThirdPersonCamera();
            Vector3fc feet = player.position();
            viewPose = new ViewPose.ThirdPerson(new PlayerModelState(
                    feet.x(), feet.y(), feet.z(),
                    player.yaw(), player.pitch(),
                    swingPhase, swingAmplitude, attackSwing));
        } else {
            renderer.camera().setPose(player.eyePosition(), player.yaw(), player.pitch());
            viewPose = new ViewPose.FirstPerson(attackSwing);
        }
        renderer.render((float) width / height, dayNightCycle, viewPose);
    }

    /**
     * Puts the camera behind the player's eye along the view direction,
     * pulled in so it never ends up inside terrain.
     */
    private void placeThirdPersonCamera() {
        Vector3fc eye = player.eyePosition();
        player.lookDirection(cameraDirection).negate(); // unit vector from eye toward the camera
        float distance = THIRD_PERSON_DISTANCE;
        Optional<RayHit> wall = VoxelRaycaster.raycast(
                world, eye, cameraDirection, THIRD_PERSON_DISTANCE + CAMERA_COLLISION_MARGIN);
        if (wall.isPresent()) {
            float free = VoxelRaycaster.hitDistance(wall.get(), eye, cameraDirection) - CAMERA_COLLISION_MARGIN;
            distance = Math.clamp(free, MIN_CAMERA_DISTANCE, THIRD_PERSON_DISTANCE);
        }
        cameraPosition.set(cameraDirection).mul(distance).add(eye);
        renderer.camera().setPose(cameraPosition, player.yaw(), player.pitch());
    }

    @Override
    public void close() {
        audio.close();
        renderer.close();
        world.close();
    }
}
