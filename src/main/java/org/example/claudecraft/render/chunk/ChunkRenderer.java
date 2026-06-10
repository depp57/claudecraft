package org.example.claudecraft.render.chunk;

import org.example.claudecraft.render.Mesh;
import org.example.claudecraft.render.ShaderProgram;
import org.example.claudecraft.world.ChunkPos;
import org.example.claudecraft.world.World;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Uploads one GPU mesh per loaded chunk and draws them all, positioning each
 * via the {@code uChunkOrigin} uniform so mesh vertices stay chunk-local.
 * Meshes are built eagerly at construction; remeshing on block edits and
 * frustum culling come later.
 *
 * <p>Owns the chunk meshes; release them with {@link #close()}. Render thread
 * only.
 */
public final class ChunkRenderer implements AutoCloseable {

    private static final String CHUNK_ORIGIN_UNIFORM = "uChunkOrigin";

    private final Map<ChunkPos, Mesh> meshes = new LinkedHashMap<>();

    public ChunkRenderer(World world, ChunkMesher mesher) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(mesher, "mesher");
        for (ChunkPos position : world.chunkPositions()) {
            world.chunk(position).ifPresent(chunk -> {
                MeshData data = mesher.mesh(chunk);
                if (!data.isEmpty()) {
                    meshes.put(position, new Mesh(data.vertices(), data.indices()));
                }
            });
        }
    }

    /** Draws all chunk meshes; the shader must already be bound. */
    public void draw(ShaderProgram shader) {
        for (Map.Entry<ChunkPos, Mesh> entry : meshes.entrySet()) {
            ChunkPos position = entry.getKey();
            shader.setUniform(CHUNK_ORIGIN_UNIFORM, position.minBlockX(), 0.0f, position.minBlockZ());
            entry.getValue().draw();
        }
    }

    @Override
    public void close() {
        meshes.values().forEach(Mesh::close);
        meshes.clear();
    }
}
