#version 330 core

in vec2 vTexCoord;
flat in float vLight;
in float vFogDistance;

uniform sampler2D uTexture;
uniform vec3 uFogColor;
uniform float uFogStart;
uniform float uFogEnd;

out vec4 fragColor;

void main() {
    vec4 texel = texture(uTexture, vTexCoord);
    vec3 lit = texel.rgb * vLight;
    float fog = smoothstep(uFogStart, uFogEnd, vFogDistance);
    fragColor = vec4(mix(lit, uFogColor, fog), 1.0);
}
