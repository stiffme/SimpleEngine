#version 330 core

//this renderer is plain
//no light render, just ambient
in vec2 oTexcoord;
uniform sampler2D ambient;
uniform sampler2D diffuse;
out vec4 oColor;
void main() {
    vec3 ambientColor = texture(ambient, oTexcoord).rgb;
    vec3 diffuseColor = texture(diffuse, oTexcoord).rgb;
    oColor = vec4(ambientColor + diffuseColor, 1.0);
}
