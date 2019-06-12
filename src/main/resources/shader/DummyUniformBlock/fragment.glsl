#version 330

out vec4 oColor;

uniform Light {
    vec3 lightPos;
    float quadratic;
    vec3 lightDir;
    float constant;
    vec3 lAmbient;
    float linear;
    vec3 lDiffuse;
    vec3 lSpecular;
} light;

uniform ViewPos {
    vec3 viewPos;
};

void main() {
    oColor = vec4(light.lightPos * viewPos, light.constant);
}
