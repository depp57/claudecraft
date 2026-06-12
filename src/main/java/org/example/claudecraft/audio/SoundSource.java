package org.example.claudecraft.audio;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

/**
 * An OpenAL source playing one {@link SoundBuffer} at a time, positioned at
 * the listener (sounds are non-spatial). Must be used on the audio thread.
 */
public final class SoundSource implements AutoCloseable {

    private final int handle;

    public SoundSource(float gain) {
        handle = alGenSources();
        alSourcef(handle, AL_GAIN, gain);
    }

    /** Stops whatever is playing and starts the given buffer from the top. */
    public void play(SoundBuffer buffer) {
        alSourceStop(handle);
        alSourcei(handle, AL_BUFFER, buffer.handle());
        alSourcePlay(handle);
    }

    public boolean isPlaying() {
        return alGetSourcei(handle, AL_SOURCE_STATE) == AL_PLAYING;
    }

    @Override
    public void close() {
        alSourceStop(handle);
        alDeleteSources(handle);
    }
}
