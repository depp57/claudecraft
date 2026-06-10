package org.example.claudecraft;

import org.example.claudecraft.core.Game;
import org.example.claudecraft.core.Input;
import org.example.claudecraft.core.Window;
import org.example.claudecraft.player.BlockInteraction;
import org.example.claudecraft.player.Player;
import org.example.claudecraft.player.PlayerController;
import org.example.claudecraft.render.Renderer;
import org.example.claudecraft.world.ChunkPos;
import org.example.claudecraft.world.DayNightCycle;
import org.example.claudecraft.world.StreamingWorld;
import org.example.claudecraft.world.gen.NoiseTerrainGenerator;
import org.joml.Vector3f;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

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
    private static final float DAY_LENGTH_SECONDS = 60.0f;
    /** Start mid-morning so the world is well-lit on launch. */
    private static final float DAY_START_FRACTION = 0.1f;

    private final Window window;
    private final Input input;
    private final StreamingWorld world;
    private final Player player;
    private final PlayerController controller;
    private final Renderer renderer;
    private final BlockInteraction interaction;
    private final DayNightCycle dayNightCycle = new DayNightCycle(DAY_LENGTH_SECONDS, DAY_START_FRACTION);

    public ClaudecraftGame(Window window) {
        this.window = Objects.requireNonNull(window, "window");
        this.input = new Input(window);
        this.world = new StreamingWorld(new NoiseTerrainGenerator(WORLD_SEED), LOAD_RADIUS_CHUNKS);
        this.player = new Player(SPAWN);
        this.controller = new PlayerController(player, input, world);
        this.renderer = new Renderer(world);
        this.interaction = new BlockInteraction(world, player, input, renderer::onBlockChanged);
        world.addListener(renderer);
    }

    @Override
    public void update(float dt) {
        input.beginTick();
        if (input.isKeyDown(GLFW_KEY_ESCAPE)) {
            window.requestClose();
        }
        controller.update(dt);
        interaction.update();
        world.update(playerChunk());
        dayNightCycle.update(dt);

        if (player.position().y() < VOID_RESET_Y) {
            player.setPosition(SPAWN.x, SPAWN.y, SPAWN.z);
            player.velocity().zero();
        }
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
        renderer.camera().setPose(player.eyePosition(), player.yaw(), player.pitch());
        renderer.render((float) width / height, dayNightCycle);
    }

    @Override
    public void close() {
        renderer.close();
        world.close();
    }
}
