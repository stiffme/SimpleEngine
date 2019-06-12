#version 330 core

layout (location = 0) out vec3 gPosition;
layout (location = 1) out vec4 gNormal;
layout (location = 2) out vec3 gAmbient;
layout (location = 3) out vec4 gAlbedoSpec;


in VS_OUT{
    vec3 FragPos;
    vec2 Texcoords;
    mat3 TBN;
    //vec3 testNormal;
} fs_in;

uniform sampler2D texAmbient;
uniform sampler2D texDiffuse;
uniform sampler2D texSpecular;
uniform sampler2D texNormal;

uniform float shininess;

void main() {
    gPosition = fs_in.FragPos;

    //texture is from 0 to 2, map it to -1, 1
    vec3 normalInTangent = normalize(texture(texNormal, fs_in.Texcoords).rgb * 2f - 1f);

    gNormal.rgb = normalize(fs_in.TBN * normalInTangent);
    gNormal.a = shininess ;

    gAmbient = texture(texAmbient, fs_in.Texcoords).rgb;
    gAlbedoSpec.rgb = texture(texDiffuse, fs_in.Texcoords).rgb;
    gAlbedoSpec.a = texture(texSpecular, fs_in.Texcoords).r;
}
