#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

uniform mat4 uViewProjection;
uniform vec3 uChunkOrigin;
// Normalized direction the sunlight travels (from sun into the world).
uniform vec3 uSunDirection;

out vec2 vTexCoord;
// Normals are constant per face, so the light level is too.
flat out float vLight;

const float AMBIENT = 0.45;
const float SUN_STRENGTH = 0.55;

void main() {
    vTexCoord = aTexCoord;
    vLight = AMBIENT + SUN_STRENGTH * max(dot(aNormal, -uSunDirection), 0.0);
    gl_Position = uViewProjection * vec4(aPosition + uChunkOrigin, 1.0);
}
