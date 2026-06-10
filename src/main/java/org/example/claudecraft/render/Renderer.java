package org.example.claudecraft.render;

import org.example.claudecraft.render.chunk.ChunkRenderer;
import org.example.claudecraft.render.chunk.CullingChunkMesher;
import org.example.claudecraft.world.World;

import java.util.Objects;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glEnable;

/**
 * Top-level renderer owning the shader pipeline, texture atlas, camera and
 * chunk meshes for the given world.
 *
 * <p>Owns GL resources; release them with {@link #close()}. Render thread only.
 */
public final class Renderer implements AutoCloseable {

    private static final float SKY_RED = 0.47f;
    private static final float SKY_GREEN = 0.71f;
    private static final float SKY_BLUE = 0.99f;

    private static final int ATLAS_TEXTURE_UNIT = 0;

    private final ShaderProgram shader;
    private final Texture atlas;
    private final ChunkRenderer chunkRenderer;
    private final Camera camera = new Camera();

    public Renderer(World world) {
        Objects.requireNonNull(world, "world");

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glClearColor(SKY_RED, SKY_GREEN, SKY_BLUE, 1.0f);

        shader = ShaderProgram.load("/shaders/chunk.vert", "/shaders/chunk.frag");
        atlas = Texture.loadFromClasspath("/textures/atlas.png");
        chunkRenderer = new ChunkRenderer(world, new CullingChunkMesher());
    }

    /** The camera whose pose callers update before each frame. */
    public Camera camera() {
        return camera;
    }

    /**
     * Renders one frame from the camera's current pose.
     *
     * @param aspectRatio framebuffer width / height, must be positive
     */
    public void render(float aspectRatio) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shader.bind();
        shader.setUniform("uViewProjection", camera.viewProjection(aspectRatio));
        shader.setUniform("uTexture", ATLAS_TEXTURE_UNIT);
        atlas.bind(ATLAS_TEXTURE_UNIT);
        chunkRenderer.draw(shader);
        shader.unbind();
    }

    @Override
    public void close() {
        chunkRenderer.close();
        atlas.close();
        shader.close();
    }
}
