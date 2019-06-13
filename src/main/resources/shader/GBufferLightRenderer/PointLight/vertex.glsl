#version 330 core
layout(location=0) in mat4 aModel;
layout(location=4) in vec3 aPos;

uniform MVP {
//mat4 model;
    mat4 view;
    mat4 projection;
};

void main() {
    gl_Position = projection * view * aModel * vec4(aPos,1.0f);
}
