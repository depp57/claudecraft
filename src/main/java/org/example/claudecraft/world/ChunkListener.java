package org.example.claudecraft.world;

/**
 * Notified when chunks enter or leave the loaded set. Callbacks run on the
 * thread driving {@code StreamingWorld#update} (the main thread), never on
 * generation workers.
 */
public interface ChunkListener {

    /** Called after the chunk at the given position became available. */
    void onChunkLoaded(ChunkPos position);

    /** Called after the chunk at the given position was removed. */
    void onChunkUnloaded(ChunkPos position);
}
