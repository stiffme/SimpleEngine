#version 330 core

in vec2 oTexcoord;
out vec4 oColor;

uniform sampler2D textTexture;
void main() {
    oColor = vec4(texture(textTexture, oTexcoord).rrr, 1.0);
    //sswsaoColor = vec4(1.0,1.0,0.0,1.0);
}
