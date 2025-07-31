#version 150

#moj_import <minecraft:fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float GameTime;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

// https://thebookofshaders.com/log/161119150756.png
float random(in vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

void main() {
    vec2 st = texCoord0.xy;
    float animation = GameTime * 2000.0;
    float animation1 = sin(animation) + 1.0;

    vec4 color = vec4(0, animation1 * 0.15 + 0.85, animation1 * 0.15 + 0.85, 0.8) * ColorModulator;
    vec2 center = vec2(0.5, 0.5);
    float radius = 0.47 + abs(sin(GameTime / 2.0)) * 0.025 + abs(random(st)) * 0.001;
    float radiusSquared = pow(radius, 2);
    float distanceFromCenterSquared = pow(st.x - center.x, 2.0) + pow(st.y - center.y, 2.0);

    if (distanceFromCenterSquared >= radiusSquared)
        discard;

    float decay = mix(1.0, 0.0, distanceFromCenterSquared / radiusSquared);
    color.a += 0.5 * decay * (random(st * 50.0 + vec2(GameTime)) - 0.5);
    color.a *= decay;
    color.a *= vertexColor.a;
    fragColor = color;
}
