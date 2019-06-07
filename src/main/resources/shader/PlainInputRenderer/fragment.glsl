#version 330 core

//this renderer is plain
//no light render, just ambient
in vec2 oTexcoord;
uniform sampler2D ambient;

out vec4 oColor;
void main() {
    oColor = vec4(texture(ambient, oTexcoord).rgb, 1.0);
}
