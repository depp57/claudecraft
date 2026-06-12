#version 330 core

in vec2 vTexCoord;
in vec3 vNormal;

uniform sampler2D uSkin;
// Normalized direction the sunlight travels (from sun into the world).
uniform vec3 uSunDirection;
uniform float uAmbient;
uniform float uSunStrength;

out vec4 fragColor;

void main() {
    vec4 texel = texture(uSkin, vTexCoord);
    if (texel.a < 0.5) {
        discard;
    }
    vec3 normal = normalize(vNormal);
    float light = uAmbient + uSunStrength * max(dot(normal, -uSunDirection), 0.0);
    fragColor = vec4(texel.rgb * light, 1.0);
}
