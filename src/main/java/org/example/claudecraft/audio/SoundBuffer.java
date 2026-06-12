package org.example.claudecraft.audio;

import org.example.claudecraft.util.Resources;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.libc.LibCStdlib.free;

/**
 * An OpenAL buffer holding the fully decoded PCM of one Ogg Vorbis classpath
 * resource. Must be created and closed on the audio thread (the thread that
 * owns the {@link AudioDevice} context).
 */
public final class SoundBuffer implements AutoCloseable {

    private final int handle;

    /**
     * Decodes the given Ogg Vorbis resource into a new buffer.
     *
     * @param resourcePath absolute classpath location, e.g. {@code /sounds/music/music-1.ogg}
     * @throws AudioException if the resource is not valid Ogg Vorbis
     */
    public SoundBuffer(String resourcePath) {
        byte[] bytes = Resources.readBytes(resourcePath);
        ByteBuffer encoded = MemoryUtil.memAlloc(bytes.length).put(bytes).flip();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer channels = stack.mallocInt(1);
            IntBuffer sampleRate = stack.mallocInt(1);
            ShortBuffer pcm = stb_vorbis_decode_memory(encoded, channels, sampleRate);
            if (pcm == null) {
                throw new AudioException("Failed to decode Ogg Vorbis resource: " + resourcePath);
            }
            int format = channels.get(0) == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
            handle = alGenBuffers();
            alBufferData(handle, format, pcm, sampleRate.get(0));
            free(pcm);
        } finally {
            MemoryUtil.memFree(encoded);
        }
    }

    int handle() {
        return handle;
    }

    @Override
    public void close() {
        alDeleteBuffers(handle);
    }
}
