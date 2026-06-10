package org.example.claudecraft.render;

import org.example.claudecraft.util.ResourceLoadException;
import org.example.claudecraft.util.Resources;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

/**
 * A 2D OpenGL texture loaded from an image resource. Uses nearest-neighbor
 * filtering for the crisp pixel-art look; no mipmaps for now (they would
 * bleed across atlas tile borders without padding).
 *
 * <p>Owns the GL texture object; release it with {@link #close()}. Render
 * thread only.
 */
public final class Texture implements AutoCloseable {

    private final int textureId;

    private Texture(int textureId) {
        this.textureId = textureId;
    }

    /**
     * Decodes an image from the classpath (PNG and other stb-supported
     * formats) and uploads it as an RGBA texture.
     *
     * @throws ResourceLoadException if the resource is missing or not a decodable image
     */
    public static Texture loadFromClasspath(String path) {
        byte[] bytes = Resources.readBytes(path);
        ByteBuffer encoded = MemoryUtil.memAlloc(bytes.length).put(bytes).flip();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            ByteBuffer pixels = stbi_load_from_memory(encoded, width, height, channels, 4);
            if (pixels == null) {
                throw new ResourceLoadException("Failed to decode image " + path + ": " + stbi_failure_reason());
            }
            try {
                return upload(pixels, width.get(0), height.get(0));
            } finally {
                stbi_image_free(pixels);
            }
        } finally {
            MemoryUtil.memFree(encoded);
        }
    }

    private static Texture upload(ByteBuffer pixels, int width, int height) {
        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        glBindTexture(GL_TEXTURE_2D, 0);
        return new Texture(id);
    }

    /** Binds this texture to the given texture unit (0-based). */
    public void bind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    @Override
    public void close() {
        glDeleteTextures(textureId);
    }
}
