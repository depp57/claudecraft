package org.example.claudecraft.render;

import org.example.claudecraft.render.chunk.ChunkRenderer;
import org.example.claudecraft.render.chunk.CullingChunkMesher;
import org.example.claudecraft.world.ChunkListener;
import org.example.claudecraft.world.ChunkPos;
import org.example.claudecraft.world.DayNightCycle;
import org.example.claudecraft.world.World;
import org.joml.FrustumIntersection;
import org.joml.Vector3f;

import java.util.Objects;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glEnable;

/**
 * Top-level renderer owning the shader pipeline, texture atlas, camera, sky
 * and chunk meshes for the given world. Implements {@link ChunkListener} so a
 * streaming world can drive chunk mesh creation and removal.
 *
 * <p>Owns GL resources; release them with {@link #close()}. Render thread only.
 */
public final class Renderer implements ChunkListener, AutoCloseable {

    private static final int ATLAS_TEXTURE_UNIT = 0;

    // Sky/light presentation, blended by the cycle's daylight factor.
    private static final Vector3f ZENITH_DAY = new Vector3f(0.25f, 0.55f, 0.95f);
    private static final Vector3f ZENITH_NIGHT = new Vector3f(0.02f, 0.03f, 0.08f);
    private static final Vector3f HORIZON_DAY = new Vector3f(0.72f, 0.82f, 0.95f);
    private static final Vector3f HORIZON_NIGHT = new Vector3f(0.05f, 0.06f, 0.12f);
    private static final float AMBIENT_NIGHT = 0.16f;
    private static final float AMBIENT_DAY = 0.45f;
    private static final float SUN_STRENGTH_DAY = 0.55f;

    // Fog hides the edge of the loaded area (radius 8 chunks = 128 blocks).
    private static final float FOG_START = 96.0f;
    private static final float FOG_END = 140.0f;

    private final World world;
    private final ShaderProgram shader;
    private final Texture atlas;
    private final ChunkRenderer chunkRenderer;
    private final SkyRenderer sky;
    private final CrosshairRenderer crosshair;
    private final Camera camera = new Camera();
    /** Reused every frame; render is a hot path and must not allocate. */
    private final FrustumIntersection frustum = new FrustumIntersection();
    private final Vector3f sunDirection = new Vector3f();
    private final Vector3f zenithColor = new Vector3f();
    private final Vector3f horizonColor = new Vector3f();

    public Renderer(World world) {
        this.world = Objects.requireNonNull(world, "world");

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        shader = ShaderProgram.load("/shaders/chunk.vert", "/shaders/chunk.frag");
        atlas = Texture.loadFromClasspath("/textures/atlas.png");
        chunkRenderer = new ChunkRenderer(world, new CullingChunkMesher());
        sky = new SkyRenderer();
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
     * Renders one frame from the camera's current pose at the given time of
     * day.
     *
     * @param aspectRatio framebuffer width / height, must be positive
     */
    public void render(float aspectRatio, DayNightCycle cycle) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        chunkRenderer.uploadCompleted();

        float daylight = cycle.daylight();
        cycle.sunDirection(sunDirection);
        zenithColor.set(ZENITH_NIGHT).lerp(ZENITH_DAY, daylight);
        horizonColor.set(HORIZON_NIGHT).lerp(HORIZON_DAY, daylight);

        var viewProjection = camera.viewProjection(aspectRatio);
        frustum.set(viewProjection);

        sky.draw(camera.skyViewProjectionInverse(aspectRatio), sunDirection, zenithColor, horizonColor, daylight);

        shader.bind();
        shader.setUniform("uViewProjection", viewProjection);
        shader.setUniform("uSunDirection", sunDirection);
        shader.setUniform("uAmbient", lerp(AMBIENT_NIGHT, AMBIENT_DAY, daylight));
        shader.setUniform("uSunStrength", SUN_STRENGTH_DAY * daylight);
        shader.setUniform("uCameraPosition", camera.position());
        shader.setUniform("uFogColor", horizonColor);
        shader.setUniform("uFogStart", FOG_START);
        shader.setUniform("uFogEnd", FOG_END);
        shader.setUniform("uTexture", ATLAS_TEXTURE_UNIT);
        atlas.bind(ATLAS_TEXTURE_UNIT);
        chunkRenderer.draw(shader, frustum);
        shader.unbind();

        crosshair.draw(aspectRatio);
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    @Override
    public void close() {
        crosshair.close();
        sky.close();
        chunkRenderer.close();
        atlas.close();
        shader.close();
    }
}
