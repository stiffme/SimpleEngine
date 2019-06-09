#version 330 core

//this renderer is plain
//no light render, just ambient

layout(location=0) in mat4 model;
layout(location=4) in vec3 aPos;
layout(location=5) in vec2 aTexcoord;

out vec2 oTexcoord;

uniform MVP {
    //mat4 model;
    mat4 view;
    mat4 projection;
};

void main() {
    oTexcoord = aTexcoord;
    gl_Position = projection * view * model * vec4(aPos, 1.0);
}
