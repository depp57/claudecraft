package org.example.claudecraft.audio;

import org.example.claudecraft.world.BlockType;

/**
 * High-level game audio: background music plus dig/step/jump effects keyed by
 * the block surface they happen on. Block types without sounds (air) are
 * silently ignored. All methods must be called from the game-loop thread.
 */
public interface SoundSystem extends AutoCloseable {

    /** Plays a dig sound for breaking a block of the given type. */
    void playDig(BlockType type);

    /** Plays a footstep sound for walking on a block of the given type. */
    void playStep(BlockType type);

    /** Plays a jump sound for jumping off a block of the given type. */
    void playJump(BlockType type);

    /** Keeps background music rotating; call once per simulation tick. */
    void update();

    @Override
    void close();

    /** A no-op system, used when no audio device is available. */
    static SoundSystem silent() {
        return new SoundSystem() {
            @Override
            public void playDig(BlockType type) {
            }

            @Override
            public void playStep(BlockType type) {
            }

            @Override
            public void playJump(BlockType type) {
            }

            @Override
            public void update() {
            }

            @Override
            public void close() {
            }
        };
    }
}
