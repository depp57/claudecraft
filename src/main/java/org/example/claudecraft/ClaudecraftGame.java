package org.example.claudecraft;

import org.example.claudecraft.core.Game;
import org.example.claudecraft.core.Input;
import org.example.claudecraft.core.Window;
import org.example.claudecraft.player.Player;
import org.example.claudecraft.player.PlayerController;
import org.example.claudecraft.render.Renderer;
import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.Chunk;
import org.example.claudecraft.world.GeneratedWorld;
import org.example.claudecraft.world.World;
import org.example.claudecraft.world.gen.NoiseTerrainGenerator;
import org.joml.Vector3f;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Root game object wiring world, input, simulation and rendering together.
 * The player flies freely (WASD + mouse look, Space/Shift for up/down) over
 * noise-generated hills; ESC exits.
 */
public final class ClaudecraftGame implements Game, AutoCloseable {

    private static final long WORLD_SEED = 20260610L;
    private static final int WORLD_RADIUS_CHUNKS = 4;
    private static final float SPAWN_EYE_HEIGHT = 3.0f;

    private final Window window;
    private final Input input;
    private final World world;
    private final Player player;
    private final PlayerController controller;
    private final Renderer renderer;

    public ClaudecraftGame(Window window) {
        this.window = Objects.requireNonNull(window, "window");
        this.input = new Input(window);
        this.world = new GeneratedWorld(new NoiseTerrainGenerator(WORLD_SEED), WORLD_RADIUS_CHUNKS);
        this.player = new Player(spawnPoint(world));
        this.controller = new PlayerController(player, input);
        this.renderer = new Renderer(world);
    }

    /** Eye position above the terrain surface in the center of the world. */
    private static Vector3f spawnPoint(World world) {
        for (int y = Chunk.SIZE_Y - 1; y >= 0; y--) {
            if (world.block(8, y, 8) != BlockType.AIR) {
                return new Vector3f(8.5f, y + 1 + SPAWN_EYE_HEIGHT, 8.5f);
            }
        }
        return new Vector3f(8.5f, Chunk.SIZE_Y / 2.0f, 8.5f); // void world; float mid-air
    }

    @Override
    public void update(float dt) {
        input.beginTick();
        if (input.isKeyDown(GLFW_KEY_ESCAPE)) {
            window.requestClose();
        }
        controller.update(dt);
    }

    @Override
    public void render(float alpha) {
        int width = window.framebufferWidth();
        int height = window.framebufferHeight();
        if (width == 0 || height == 0) {
            return; // minimized
        }
        renderer.camera().setPose(player.position(), player.yaw(), player.pitch());
        renderer.render((float) width / height);
    }

    @Override
    public void close() {
        renderer.close();
    }
}
