#version 330 core
layout(location=0) in mat4 aModel;
layout(location=4) in vec3 aPos;
layout(location=5) in vec2 aTexcoord;
layout(location=6) in vec3 aNormal;
layout(location=7) in vec3 aTangent;
layout(location=8) in vec3 aBitangent;

out VS_OUT{
    vec3 FragPos;
    vec2 Texcoords;
    mat3 TBN;
} vs_out;

uniform MVP {
//mat4 model;
    mat4 view;
    mat4 projection;
};


void main() {
    vs_out.FragPos = aModel * vec4(aPos, 1.0);
    vs_out.Texcoords = aTexcoord;
    mat3 normalMatrix = transpose(inverse(mat3(aModel))) ;
    vec3 N = normalize(normalMatrix * aNormal);
    vec3 T = normalize(normalMatrix * aTangent);
    vec3 B = normalize(normalMatrix * aBitangent);
    vs_out.TBN = mat3(T, B, N);

    gl_Position = projection * view * aModel * vec4(aPos, 1.0);
}
