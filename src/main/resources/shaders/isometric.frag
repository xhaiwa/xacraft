#version 330 core

in vec2 texCoord;
in float shade;

out vec4 color;

uniform sampler2D textureSampler;

void main() {
    vec4 texColor = texture(textureSampler, texCoord);
    if (texColor.a < 0.1) discard;
    color = vec4(texColor.rgb * shade, texColor.a);
}