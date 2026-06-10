package org.example.claudecraft.render;

import org.example.claudecraft.render.chunk.CullingChunkMesher;
import org.example.claudecraft.render.chunk.MeshData;
import org.example.claudecraft.world.Chunk;
import org.example.claudecraft.world.gen.FlatTerrainGenerator;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glEnable;

/**
 * Top-level renderer owning the shader pipeline. Currently draws one
 * flat-terrain chunk from a fixed viewpoint; the first-person camera and
 * multi-chunk rendering will replace the hardcoded view.
 *
 * <p>Owns GL resources; release them with {@link #close()}. Render thread only.
 */
public final class Renderer implements AutoCloseable {

    private static final float SKY_RED = 0.47f;
    private static final float SKY_GREEN = 0.71f;
    private static final float SKY_BLUE = 0.99f;

    private static final float FOV_RADIANS = (float) Math.toRadians(70.0);
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 1000.0f;
    private static final int GROUND_HEIGHT = 64;

    private final ShaderProgram shader;
    private final Mesh chunkMesh;
    /** Reused every frame; render is a hot path and must not allocate. */
    private final Matrix4f viewProjection = new Matrix4f();

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

    /**
     * Renders one frame.
     *
     * @param aspectRatio framebuffer width / height, must be positive
     */
    public void render(float aspectRatio) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        viewProjection.setPerspective(FOV_RADIANS, aspectRatio, NEAR_PLANE, FAR_PLANE)
                .lookAt(-14.0f, GROUND_HEIGHT + 14.0f, -14.0f,
                        Chunk.SIZE_X / 2.0f, GROUND_HEIGHT, Chunk.SIZE_Z / 2.0f,
                        0.0f, 1.0f, 0.0f);

        shader.bind();
        shader.setUniform("uViewProjection", viewProjection);
        chunkMesh.draw();
        shader.unbind();
    }

    @Override
    public void close() {
        chunkMesh.close();
        shader.close();
    }
}
