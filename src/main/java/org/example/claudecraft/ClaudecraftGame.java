package org.example.claudecraft;

import org.example.claudecraft.core.Game;
import org.example.claudecraft.core.Window;
import org.example.claudecraft.render.Renderer;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Root game object wiring input, simulation and rendering together.
 * Currently renders a flat-terrain chunk and exits on ESC.
 */
public final class ClaudecraftGame implements Game, AutoCloseable {

    private final Window window;
    private final Renderer renderer;

    public ClaudecraftGame(Window window) {
        this.window = Objects.requireNonNull(window, "window");
        this.renderer = new Renderer();
    }

    @Override
    public void update(float dt) {
        if (window.isKeyPressed(GLFW_KEY_ESCAPE)) {
            window.requestClose();
        }
    }

    @Override
    public void render(float alpha) {
        int width = window.framebufferWidth();
        int height = window.framebufferHeight();
        if (width == 0 || height == 0) {
            return; // minimized
        }
        renderer.render((float) width / height);
    }

    @Override
    public void close() {
        renderer.close();
    }
}
