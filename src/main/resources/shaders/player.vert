#version 330 core

// Locations 0-2 of the shared mesh layout; location 3 (sky light) is unused —
// the player model is always fully sky-lit.
layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

uniform mat4 uViewProjection;
uniform mat4 uModel;

out vec2 vTexCoord;
out vec3 vNormal;

void main() {
    vTexCoord = aTexCoord;
    vNormal = mat3(uModel) * aNormal;
    gl_Position = uViewProjection * uModel * vec4(aPosition, 1.0);
}
