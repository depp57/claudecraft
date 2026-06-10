package org.example.claudecraft.render;

import org.example.claudecraft.render.chunk.CullingChunkMesher;
import org.example.claudecraft.render.chunk.MeshData;
import org.example.claudecraft.world.Chunk;
import org.example.claudecraft.world.gen.FlatTerrainGenerator;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glEnable;

/**
 * Top-level renderer owning the shader pipeline and the camera. Currently
 * draws one flat-terrain chunk; multi-chunk rendering will replace the single
 * mesh.
 *
 * <p>Owns GL resources; release them with {@link #close()}. Render thread only.
 */
public final class Renderer implements AutoCloseable {

    private static final float SKY_RED = 0.47f;
    private static final float SKY_GREEN = 0.71f;
    private static final float SKY_BLUE = 0.99f;

    public static final int GROUND_HEIGHT = 64;

    private final ShaderProgram shader;
    private final Mesh chunkMesh;
    private final Camera camera = new Camera();

    public Renderer() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glClearColor(SKY_RED, SKY_GREEN, SKY_BLUE, 1.0f);

        shader = ShaderProgram.load("/shaders/basic.vert", "/shaders/basic.frag");

        Chunk chunk = new Chunk();
        new FlatTerrainGenerator(GROUND_HEIGHT).generate(chunk);
        MeshData meshData = new CullingChunkMesher().mesh(chunk);
        chunkMesh = new Mesh(meshData.vertices(), meshData.indices());
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
        chunkMesh.draw();
        shader.unbind();
    }

    @Override
    public void close() {
        chunkMesh.close();
        shader.close();
    }
}
