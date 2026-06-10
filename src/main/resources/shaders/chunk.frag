#version 330 core

in vec2 vTexCoord;
flat in float vLight;

uniform sampler2D uTexture;

out vec4 fragColor;

void main() {
    vec4 texel = texture(uTexture, vTexCoord);
    fragColor = vec4(texel.rgb * vLight, 1.0);
}
