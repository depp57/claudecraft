package org.example.claudecraft;

import org.example.claudecraft.core.Game;
import org.example.claudecraft.core.Input;
import org.example.claudecraft.core.Window;
import org.example.claudecraft.player.Player;
import org.example.claudecraft.player.PlayerController;
import org.example.claudecraft.render.Renderer;
import org.example.claudecraft.world.Chunk;
import org.joml.Vector3f;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Root game object wiring input, simulation and rendering together.
 * The player flies freely (WASD + mouse look, Space/Shift for up/down)
 * over a flat-terrain chunk; ESC exits.
 */
public final class ClaudecraftGame implements Game, AutoCloseable {

    /** Eye height above the grass surface at spawn, pre-physics placeholder. */
    private static final Vector3f SPAWN = new Vector3f(
            Chunk.SIZE_X / 2.0f, Renderer.GROUND_HEIGHT + 2.7f, Chunk.SIZE_Z / 2.0f);

    private final Window window;
    private final Input input;
    private final Player player;
    private final PlayerController controller;
    private final Renderer renderer;

    public ClaudecraftGame(Window window) {
        this.window = Objects.requireNonNull(window, "window");
        this.input = new Input(window);
        this.player = new Player(SPAWN);
        this.controller = new PlayerController(player, input);
        this.renderer = new Renderer();
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
