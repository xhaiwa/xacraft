#version 330 core

in vec3 fragColor;
in float fragDistance;
in vec2 texCoord;

out vec4 color;

uniform sampler2D textureSampler;
uniform vec3 fogColor = vec3(0.5, 0.7, 1.0);
uniform float fogDensity = 0.002;

void main() {
    vec4 texColor = texture(textureSampler, texCoord);

    vec3 litColor = texColor.rgb * fragColor;

    float fogFactor = exp(-fogDensity * fragDistance);
    fogFactor = clamp(fogFactor, 0.0, 1.0);
    vec3 finalColor = mix(fogColor, litColor, fogFactor);

    float finalAlpha = texColor.a;

    finalAlpha *= fogFactor;

    color = vec4(finalColor, finalAlpha);
}
