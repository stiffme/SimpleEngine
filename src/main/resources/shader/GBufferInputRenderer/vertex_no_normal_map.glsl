#version 330 core
layout(location=0) in mat4 aModel;
layout(location=4) in vec3 aPos;
layout(location=5) in vec2 aTexcoord;
layout(location=6) in vec3 aNormal;
//we don't need tangent space because there is no normal map

out VS_OUT{
    vec3 FragPos;
    vec2 Texcoords;
    vec3 Normal;
} vs_out;

uniform MVP {
//mat4 model;
    mat4 view;
    mat4 projection;
};


void main() {
    vs_out.FragPos = vec3(aModel * vec4(aPos, 1.0));
    vs_out.Texcoords = aTexcoord;
    vs_out.Normal = transpose(inverse(mat3(aModel))) * aNormal;
    gl_Position = projection * view * aModel * vec4(aPos, 1.0);
}
