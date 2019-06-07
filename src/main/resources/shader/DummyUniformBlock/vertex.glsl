#version 330 core

//this is a dummy program for detecting uniform block

uniform MVP {
    mat4 model;
    mat4 view;
    mat4 projection;
};

void main() {
    gl_Position = projection * view * model * vec4(1,1,1,1);
}
