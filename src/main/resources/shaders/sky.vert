#version 330 core

layout (location = 0) in vec2 aPosition;

out vec2 vNdc;

void main() {
    vNdc = aPosition;
    gl_Position = vec4(aPosition, 0.0, 1.0);
}
