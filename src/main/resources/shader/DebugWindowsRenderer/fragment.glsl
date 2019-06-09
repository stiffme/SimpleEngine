#version 330 core

uniform sampler2D debugTexture;
uniform float debugAlpha;
in vec2 oTexcoord;
out vec4 oColor;

void main() {
    oColor = texture(debugTexture, oTexcoord);
    oColor.a = debugAlpha;
}
