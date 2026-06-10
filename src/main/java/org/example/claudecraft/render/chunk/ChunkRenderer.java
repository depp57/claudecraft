package org.example.claudecraft.render.chunk;

import org.example.claudecraft.render.Mesh;
import org.example.claudecraft.render.ShaderProgram;
import org.example.claudecraft.world.Chunk;
import org.example.claudecraft.world.ChunkListener;
import org.example.claudecraft.world.ChunkPos;
import org.example.claudecraft.world.World;
import org.joml.FrustumIntersection;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keeps one GPU mesh per loaded chunk and draws the ones inside the view
 * frustum, positioning each via the {@code uChunkOrigin} uniform so mesh
 * vertices stay chunk-local.
 *
 * <p>Meshing of newly loaded chunks runs on a worker pool over chunk
 * snapshots; finished geometry is queued and uploaded on the render thread by
 * {@link #uploadCompleted()}, a bounded number per frame. Every (re)mesh
 * bumps a per-chunk version stamp, so async results that were overtaken by an
 * edit's synchronous {@link #remesh} — or by an unload — are discarded
 * instead of overwriting newer geometry.
 *
 * <p>Owns the chunk meshes; release them with {@link #close()}. All public
 * methods are render-thread only.
 */
public final class ChunkRenderer implements ChunkListener, AutoCloseable {

    private static final String CHUNK_ORIGIN_UNIFORM = "uChunkOrigin";
    /** Caps GL buffer uploads per frame so a burst of loaded chunks cannot cause a frame spike. */
    private static final int MAX_UPLOADS_PER_FRAME = 16;
    private static final int MESHING_THREADS = 2;

    private final World world;
    private final ChunkMesher mesher;
    private final ExecutorService meshingPool;
    private final Map<ChunkPos, Mesh> meshes = new LinkedHashMap<>();
    private final Map<ChunkPos, Integer> meshVersions = new HashMap<>();
    private final ConcurrentLinkedQueue<MeshResult> completed = new ConcurrentLinkedQueue<>();
    private int versionCounter;

    private record MeshResult(ChunkPos position, int version, MeshData data) {
    }

    public ChunkRenderer(World world, ChunkMesher mesher) {
        this.world = Objects.requireNonNull(world, "world");
        this.mesher = Objects.requireNonNull(mesher, "mesher");

        AtomicInteger threadNumber = new AtomicInteger();
        this.meshingPool = Executors.newFixedThreadPool(MESHING_THREADS, runnable -> {
            Thread thread = new Thread(runnable, "chunk-mesh-" + threadNumber.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });

        // Pre-filled worlds (tests, GeneratedWorld) get their chunks meshed
        // immediately; a StreamingWorld starts empty and streams them in.
        for (ChunkPos position : world.chunkPositions()) {
            onChunkLoaded(position);
        }
    }

    @Override
    public void onChunkLoaded(ChunkPos position) {
        world.chunk(position).ifPresent(chunk -> {
            Chunk snapshot = chunk.copy(); // workers must not observe simulation edits mid-mesh
            int version = bumpVersion(position);
            meshingPool.submit(() -> completed.add(new MeshResult(position, version, mesher.mesh(snapshot))));
        });
    }

    @Override
    public void onChunkUnloaded(ChunkPos position) {
        meshVersions.remove(position);
        Mesh mesh = meshes.remove(position);
        if (mesh != null) {
            mesh.close();
        }
    }

    /**
     * Rebuilds the mesh of one chunk synchronously, for immediate feedback
     * after a block edit. Neighbor chunks need no rebuild while the mesher
     * treats out-of-chunk blocks as air — their border faces are always
     * emitted; revisit with cross-chunk culling.
     */
    public void remesh(ChunkPos position) {
        bumpVersion(position);
        world.chunk(position).ifPresent(chunk -> upload(position, mesher.mesh(chunk)));
    }

    /** Uploads finished async meshes, at most {@value MAX_UPLOADS_PER_FRAME} per call. */
    public void uploadCompleted() {
        for (int i = 0; i < MAX_UPLOADS_PER_FRAME; i++) {
            MeshResult result = completed.poll();
            if (result == null) {
                return;
            }
            Integer currentVersion = meshVersions.get(result.position());
            if (currentVersion != null && currentVersion == result.version()) {
                upload(result.position(), result.data());
            }
        }
    }

    /** Draws all chunk meshes intersecting the frustum; the shader must already be bound. */
    public void draw(ShaderProgram shader, FrustumIntersection frustum) {
        for (Map.Entry<ChunkPos, Mesh> entry : meshes.entrySet()) {
            ChunkPos position = entry.getKey();
            float minX = position.minBlockX();
            float minZ = position.minBlockZ();
            if (!frustum.testAab(minX, 0.0f, minZ, minX + Chunk.SIZE_X, Chunk.SIZE_Y, minZ + Chunk.SIZE_Z)) {
                continue;
            }
            shader.setUniform(CHUNK_ORIGIN_UNIFORM, minX, 0.0f, minZ);
            entry.getValue().draw();
        }
    }

    private int bumpVersion(ChunkPos position) {
        versionCounter++;
        meshVersions.put(position, versionCounter);
        return versionCounter;
    }

    private void upload(ChunkPos position, MeshData data) {
        Mesh previous = meshes.remove(position);
        if (previous != null) {
            previous.close();
        }
        if (!data.isEmpty()) {
            meshes.put(position, new Mesh(data.vertices(), data.indices()));
        }
    }

    @Override
    public void close() {
        meshingPool.shutdownNow();
        meshes.values().forEach(Mesh::close);
        meshes.clear();
    }
}
