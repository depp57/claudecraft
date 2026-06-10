package org.example.claudecraft.render;

import org.example.claudecraft.util.Resources;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.opengl.GL20C.*;

/**
 * A linked GLSL program (vertex + fragment shader) with cached uniform locations.
 *
 * <p>Owns the GL program object; release it with {@link #close()}. All methods
 * must be called on the render thread.
 */
public final class ShaderProgram implements AutoCloseable {

    private final int programId;
    private final Map<String, Integer> uniformLocations = new HashMap<>();

    /**
     * Compiles and links a program from two classpath resources,
     * e.g. {@code load("/shaders/basic.vert", "/shaders/basic.frag")}.
     */
    public static ShaderProgram load(String vertexResource, String fragmentResource) {
        return new ShaderProgram(Resources.readString(vertexResource), Resources.readString(fragmentResource));
    }

    /**
     * Compiles both shader stages and links them.
     *
     * @throws ShaderCompileException if compilation or linking fails; the GL error
     *                                log is included in the message
     */
    public ShaderProgram(String vertexSource, String fragmentSource) {
        Objects.requireNonNull(vertexSource, "vertexSource");
        Objects.requireNonNull(fragmentSource, "fragmentSource");

        int vertexId = compile(GL_VERTEX_SHADER, vertexSource, "vertex");
        int fragmentId;
        try {
            fragmentId = compile(GL_FRAGMENT_SHADER, fragmentSource, "fragment");
        } catch (ShaderCompileException e) {
            glDeleteShader(vertexId);
            throw e;
        }

        programId = glCreateProgram();
        glAttachShader(programId, vertexId);
        glAttachShader(programId, fragmentId);
        glLinkProgram(programId);
        glDetachShader(programId, vertexId);
        glDetachShader(programId, fragmentId);
        glDeleteShader(vertexId);
        glDeleteShader(fragmentId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(programId);
            glDeleteProgram(programId);
            throw new ShaderCompileException("Failed to link shader program: " + log);
        }
    }

    private static int compile(int type, String source, String stageName) {
        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, source);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shaderId);
            glDeleteShader(shaderId);
            throw new ShaderCompileException("Failed to compile " + stageName + " shader: " + log);
        }
        return shaderId;
    }

    /** Makes this program the active one for subsequent draw calls. */
    public void bind() {
        glUseProgram(programId);
    }

    /** Clears the active program; useful to catch draws that forgot to bind. */
    public void unbind() {
        glUseProgram(0);
    }

    public void setUniform(String name, int value) {
        glUniform1i(location(name), value);
    }

    public void setUniform(String name, float value) {
        glUniform1f(location(name), value);
    }

    public void setUniform(String name, Vector3f value) {
        glUniform3f(location(name), value.x, value.y, value.z);
    }

    public void setUniform(String name, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);
            glUniformMatrix4fv(location(name), false, buffer);
        }
    }

    private int location(String name) {
        return uniformLocations.computeIfAbsent(name, key -> {
            int location = glGetUniformLocation(programId, key);
            if (location < 0) {
                throw new IllegalArgumentException(
                        "Uniform '" + key + "' not found (misspelled, or optimized out because unused)");
            }
            return location;
        });
    }

    @Override
    public void close() {
        glDeleteProgram(programId);
    }
}
