package org.example.claudecraft.render;

/** Thrown when a GLSL shader fails to compile or a program fails to link. */
public final class ShaderCompileException extends RuntimeException {

    public ShaderCompileException(String message) {
        super(message);
    }
}
