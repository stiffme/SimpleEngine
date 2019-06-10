#version 330 core

layout (location = 0) out vec3 gPosition;
layout (location = 1) out vec3 gNormal;
layout (location = 2) out vec3 gAmbient;
layout (location = 3) out vec4 gAlbedoSpec;


in VS_OUT{
    vec3 FragPos;
    vec2 Texcoords;
    vec3 Normal;
} fs_in;

uniform sampler2D texAmbient;
uniform sampler2D texDiffuse;
uniform sampler2D texSpecular;


void main() {
    gPosition = fs_in.FragPos;
    gNormal = normalize(fs_in.Normal);
    gAmbient = texture(texAmbient, fs_in.Texcoords).rgb;
    gAlbedoSpec.rgb = texture(texDiffuse, fs_in.Texcoords).rgb;
    gAlbedoSpec.a = texture(texSpecular, fs_in.Texcoords).r;
}
