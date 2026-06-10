package org.example.claudecraft.world;

/** Notified after a block in the world has changed, e.g. to trigger remeshing. */
@FunctionalInterface
public interface BlockChangeListener {

    /** Called with the world position of a block that just changed. */
    void onBlockChanged(int worldX, int worldY, int worldZ);
}
