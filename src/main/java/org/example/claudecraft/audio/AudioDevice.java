package org.example.claudecraft.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * The default OpenAL output device and its context. Creating an instance
 * makes the context current on the calling thread; all other audio classes
 * must be used on that same thread.
 */
public final class AudioDevice implements AutoCloseable {

    private final long device;
    private final long context;

    /**
     * Opens the default output device.
     *
     * @throws AudioException if no audio device is available
     */
    public AudioDevice() {
        device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new AudioException("No OpenAL audio device available");
        }
        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        context = alcCreateContext(device, (IntBuffer) null);
        if (context == NULL) {
            alcCloseDevice(device);
            throw new AudioException("Failed to create OpenAL context");
        }
        alcMakeContextCurrent(context);
        AL.createCapabilities(alcCapabilities);
    }

    @Override
    public void close() {
        alcMakeContextCurrent(NULL);
        alcDestroyContext(context);
        alcCloseDevice(device);
    }
}
