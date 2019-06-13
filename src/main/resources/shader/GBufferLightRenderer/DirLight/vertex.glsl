#version 330 core

layout(location=4) in vec2 aPos;
layout(location=5) in vec2 aTexCoord;
out vec2 texCoord;

void main() {
    texCoord = aTexCoord;
    gl_Position = vec4(aPos.x, aPos.y, 0.0,1.0f);
}
