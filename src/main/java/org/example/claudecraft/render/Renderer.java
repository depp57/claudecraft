package org.example.claudecraft.render;

import org.example.claudecraft.render.chunk.ChunkRenderer;
import org.example.claudecraft.render.chunk.CullingChunkMesher;
import org.example.claudecraft.world.ChunkListener;
import org.example.claudecraft.world.ChunkPos;
import org.example.claudecraft.world.World;
import org.joml.FrustumIntersection;

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
 * chunk meshes for the given world. Implements {@link ChunkListener} so a
 * streaming world can drive chunk mesh creation and removal.
 *
 * <p>Owns GL resources; release them with {@link #close()}. Render thread only.
 */
public final class Renderer implements ChunkListener, AutoCloseable {

    private static final float SKY_RED = 0.47f;
    private static final float SKY_GREEN = 0.71f;
    private static final float SKY_BLUE = 0.99f;

    private static final int ATLAS_TEXTURE_UNIT = 0;

    private final World world;
    private final ShaderProgram shader;
    private final Texture atlas;
    private final ChunkRenderer chunkRenderer;
    private final CrosshairRenderer crosshair;
    private final Camera camera = new Camera();
    /** Reused every frame; render is a hot path and must not allocate. */
    private final FrustumIntersection frustum = new FrustumIntersection();

    public Renderer(World world) {
        this.world = Objects.requireNonNull(world, "world");

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glClearColor(SKY_RED, SKY_GREEN, SKY_BLUE, 1.0f);

        shader = ShaderProgram.load("/shaders/chunk.vert", "/shaders/chunk.frag");
        atlas = Texture.loadFromClasspath("/textures/atlas.png");
        chunkRenderer = new ChunkRenderer(world, new CullingChunkMesher());
        crosshair = new CrosshairRenderer();
    }

    /**
     * Rebuilds the mesh of the chunk containing the changed block. Runs GL
     * uploads, so while update and render share the main thread this may be
     * called from the simulation tick.
     */
    public void onBlockChanged(int worldX, int worldY, int worldZ) {
        chunkRenderer.remesh(ChunkPos.containing(worldX, worldZ));
    }

    @Override
    public void onChunkLoaded(ChunkPos position) {
        chunkRenderer.onChunkLoaded(position);
    }

    @Override
    public void onChunkUnloaded(ChunkPos position) {
        chunkRenderer.onChunkUnloaded(position);
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

        chunkRenderer.uploadCompleted();

        var viewProjection = camera.viewProjection(aspectRatio);
        frustum.set(viewProjection);

        shader.bind();
        shader.setUniform("uViewProjection", viewProjection);
        shader.setUniform("uTexture", ATLAS_TEXTURE_UNIT);
        atlas.bind(ATLAS_TEXTURE_UNIT);
        chunkRenderer.draw(shader, frustum);
        shader.unbind();

        crosshair.draw(aspectRatio);
    }

    @Override
    public void close() {
        crosshair.close();
        chunkRenderer.close();
        atlas.close();
        shader.close();
    }
}
