#version 330 core

in vec2 vNdc;

// Inverse of projection * rotation-only view: turns NDC into view rays.
uniform mat4 uInverseViewProjection;
uniform vec3 uSunDirection;
uniform vec3 uZenithColor;
uniform vec3 uHorizonColor;
uniform float uDaylight;

out vec4 fragColor;

const vec3 SUN_COLOR = vec3(1.0, 0.9, 0.7);

void main() {
    vec4 target = uInverseViewProjection * vec4(vNdc, 1.0, 1.0);
    vec3 direction = normalize(target.xyz / target.w);

    float height = clamp(direction.y, -1.0, 1.0);
    vec3 sky = mix(uHorizonColor, uZenithColor, smoothstep(0.0, 0.45, height));

    float towardSun = max(dot(direction, -uSunDirection), 0.0);
    sky += SUN_COLOR * pow(towardSun, 512.0) * 3.0;              // sun disc
    sky += SUN_COLOR * pow(towardSun, 8.0) * 0.15 * uDaylight;   // halo

    fragColor = vec4(sky, 1.0);
}
