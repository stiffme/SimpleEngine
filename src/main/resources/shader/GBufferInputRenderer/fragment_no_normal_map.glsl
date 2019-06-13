#version 330 core

layout (location = 1) out vec3 gPosition;
layout (location = 2) out vec4 gNormal;
layout (location = 3) out vec3 gAmbient;
layout (location = 4) out vec4 gAlbedoSpec;


in VS_OUT{
    vec3 FragPos;
    vec2 Texcoords;
    vec3 Normal;
} fs_in;

uniform sampler2D texAmbient;
uniform sampler2D texDiffuse;
uniform sampler2D texSpecular;
uniform float shininess;

void main() {
    gPosition = fs_in.FragPos;
    gNormal.rgb = normalize(fs_in.Normal.rgb);
    gNormal.a = shininess ;

    gAmbient = texture(texAmbient, fs_in.Texcoords).rgb;
    gAlbedoSpec.rgb = texture(texDiffuse, fs_in.Texcoords).rgb;
    gAlbedoSpec.a = texture(texSpecular, fs_in.Texcoords).r;
}
