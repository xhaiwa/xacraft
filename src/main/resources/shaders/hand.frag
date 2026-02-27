#version 330 core

in vec2 TexCoord;

out vec4 color;

uniform sampler2D textureSampler;

void main() {
    vec4 texColor = texture(textureSampler, TexCoord);
    if (texColor.a < 0.1) discard;
    color = texColor;
}