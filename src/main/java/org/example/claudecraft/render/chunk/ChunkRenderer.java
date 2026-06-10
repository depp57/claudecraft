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
 * Meshes are built eagerly at construction; {@link #remesh} rebuilds a single
 * chunk after block edits. Frustum culling comes later.
 *
 * <p>Owns the chunk meshes; release them with {@link #close()}. Render thread
 * only.
 */
public final class ChunkRenderer implements AutoCloseable {

    private static final String CHUNK_ORIGIN_UNIFORM = "uChunkOrigin";

    private final ChunkMesher mesher;
    private final Map<ChunkPos, Mesh> meshes = new LinkedHashMap<>();

    public ChunkRenderer(World world, ChunkMesher mesher) {
        Objects.requireNonNull(world, "world");
        this.mesher = Objects.requireNonNull(mesher, "mesher");
        for (ChunkPos position : world.chunkPositions()) {
            buildMesh(world, position);
        }
    }

    /**
     * Rebuilds the mesh of one chunk from current world data. Neighbor chunks
     * need no rebuild while the mesher treats out-of-chunk blocks as air —
     * their border faces are always emitted; revisit with cross-chunk culling.
     */
    public void remesh(World world, ChunkPos position) {
        Mesh previous = meshes.remove(position);
        if (previous != null) {
            previous.close();
        }
        buildMesh(world, position);
    }

    private void buildMesh(World world, ChunkPos position) {
        world.chunk(position).ifPresent(chunk -> {
            MeshData data = mesher.mesh(chunk);
            if (!data.isEmpty()) {
                meshes.put(position, new Mesh(data.vertices(), data.indices()));
            }
        });
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
