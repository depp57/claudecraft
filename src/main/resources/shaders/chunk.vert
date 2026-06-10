#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
// 1 if the air this face looks into can see the sky, 0 if it is covered.
layout (location = 3) in float aSkyLight;

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
    // Covered faces get no direct sun and only half the ambient.
    float ambient = uAmbient * mix(0.5, 1.0, aSkyLight);
    float sun = uSunStrength * max(dot(aNormal, -uSunDirection), 0.0) * aSkyLight;
    vLight = ambient + sun;
    vFogDistance = distance(worldPosition, uCameraPosition);
    gl_Position = uViewProjection * vec4(worldPosition, 1.0);
}
