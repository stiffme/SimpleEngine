#version 330 core

//this renderer is plain
//no light render, just ambient

layout(location=0) in vec3 aPos;
layout(location=2) in vec2 aTexcoord;

out vec2 oTexcoord;

uniform MVP {
    mat4 model;
    mat4 view;
    mat4 projection;
};

void main() {
    oTexcoord = aTexcoord;
    gl_Position = projection * view * model * vec4(aPos, 1.0);
}
