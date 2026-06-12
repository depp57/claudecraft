package org.example.claudecraft.audio;

import org.example.claudecraft.world.BlockType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * {@link SoundSystem} backed by OpenAL. Loads every sound fully into memory
 * at construction and keeps one source per role (music, dig, step, jump), so
 * a new effect of a role cuts off the previous one — fine at these lengths.
 *
 * <p>Owns the {@link AudioDevice}; construct and use on the game-loop thread.
 */
public final class OpenAlSoundSystem implements SoundSystem {

    private static final int VARIANT_COUNT = 3;
    private static final float MUSIC_GAIN = 0.7f;
    private static final float DIG_GAIN = 0.3f;
    private static final float STEP_GAIN = 0.3f;
    private static final float JUMP_GAIN = 0.05f;

    /** Effect sounds exist per surface material, not per block type. */
    private enum Surface {
        GRASS("grass"),
        STONE("stone");

        private final String directory;

        Surface(String directory) {
            this.directory = directory;
        }
    }

    private record SurfaceSounds(SoundVariants dig, SoundVariants hit, SoundVariants jump) {
    }

    private final AudioDevice device;
    private final List<SoundBuffer> ownedBuffers = new ArrayList<>();
    private final SoundVariants music;
    private final Map<Surface, SurfaceSounds> surfaceSounds = new EnumMap<>(Surface.class);
    private final SoundSource musicSource;
    private final SoundSource digSource;
    private final SoundSource stepSource;
    private final SoundSource jumpSource;
    private final RandomGenerator random = new Random();

    /**
     * Opens the default audio device and loads all bundled sounds.
     *
     * @throws AudioException if no device is available or a sound is missing
     */
    public OpenAlSoundSystem() {
        // Sources and buffers need the device's context current, so the device
        // must be fully created first.
        device = new AudioDevice();
        try {
            musicSource = new SoundSource(MUSIC_GAIN);
            digSource = new SoundSource(DIG_GAIN);
            stepSource = new SoundSource(STEP_GAIN);
            jumpSource = new SoundSource(JUMP_GAIN);
            music = loadVariants("/sounds/music/music-%d.ogg");
            for (Surface surface : Surface.values()) {
                String base = "/sounds/block/" + surface.directory + "/";
                surfaceSounds.put(surface, new SurfaceSounds(
                        loadVariants(base + "dig%d.ogg"),
                        loadVariants(base + "hit%d.ogg"),
                        loadVariants(base + "jump%d.ogg")));
            }
        } catch (RuntimeException e) {
            ownedBuffers.forEach(SoundBuffer::close);
            device.close();
            throw e;
        }
    }

    private SoundVariants loadVariants(String pathPattern) {
        List<SoundBuffer> buffers = new ArrayList<>(VARIANT_COUNT);
        for (int i = 1; i <= VARIANT_COUNT; i++) {
            SoundBuffer buffer = new SoundBuffer(pathPattern.formatted(i));
            ownedBuffers.add(buffer);
            buffers.add(buffer);
        }
        return new SoundVariants(buffers);
    }

    @Override
    public void playDig(BlockType type) {
        soundsFor(type).ifPresent(sounds -> digSource.play(sounds.dig().pick(random)));
    }

    @Override
    public void playStep(BlockType type) {
        soundsFor(type).ifPresent(sounds -> stepSource.play(sounds.hit().pick(random)));
    }

    @Override
    public void playJump(BlockType type) {
        soundsFor(type).ifPresent(sounds -> jumpSource.play(sounds.jump().pick(random)));
    }

    private Optional<SurfaceSounds> soundsFor(BlockType type) {
        Surface surface = switch (type) {
            case GRASS, DIRT -> Surface.GRASS;
            case STONE -> Surface.STONE;
            case AIR -> null;
        };
        return Optional.ofNullable(surface).map(surfaceSounds::get);
    }

    @Override
    public void update() {
        if (!musicSource.isPlaying()) {
            musicSource.play(music.pick(random));
        }
    }

    @Override
    public void close() {
        musicSource.close();
        digSource.close();
        stepSource.close();
        jumpSource.close();
        ownedBuffers.forEach(SoundBuffer::close);
        device.close();
    }
}
