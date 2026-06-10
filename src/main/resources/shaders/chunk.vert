#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

uniform mat4 uViewProjection;
uniform vec3 uChunkOrigin;
// Normalized direction the sunlight travels (from sun into the world).
uniform vec3 uSunDirection;
uniform float uAmbient;
uniform float uSunStrength;
uniform vec3 uCameraPosition;

out vec2 vTexCoord;
// Normals are constant per face, so the light level is too.
flat out float vLight;
out float vFogDistance;

void main() {
    vec3 worldPosition = aPosition + uChunkOrigin;
    vTexCoord = aTexCoord;
    vLight = uAmbient + uSunStrength * max(dot(aNormal, -uSunDirection), 0.0);
    vFogDistance = distance(worldPosition, uCameraPosition);
    gl_Position = uViewProjection * vec4(worldPosition, 1.0);
}
