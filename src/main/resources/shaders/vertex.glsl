#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aCol;
layout (location = 2) in vec2 aTexCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 fragColor;
out float fragDistance;
out vec2 texCoord;

void main() {
    vec4 worldPos = model * vec4(aPos, 1.0);
    vec4 viewPos = view * worldPos;

    fragDistance = length(viewPos.xyz);

    gl_Position = projection * viewPos;
    fragColor = aCol;
    texCoord = aTexCoord;
}