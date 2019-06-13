#version 330 core

//in vec2 texCoord;
uniform sampler2D GPosition;
uniform sampler2D GNormal;
uniform sampler2D GAmbient;
uniform sampler2D GAlbedoSpec;

uniform vec2 screenSize;

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
    vec2 texCoord = gl_FragCoord.xy / screenSize;
    vec3 FragPos = texture(GPosition, texCoord).rgb;
    vec4 gNormal = texture(GNormal, texCoord);
    vec3 normal = normalize(gNormal.rgb);

    vec4 ambientShininess = texture(GAmbient, texCoord);

    vec3 Mambient = ambientShininess.rgb;
    vec4 albedo = texture(GAlbedoSpec, texCoord);
    vec3 Mdiffuse = albedo.rgb;
    vec3 Mspecular = albedo.aaa;

    float distance = length(light.lightPos - FragPos);
    float attenuation = 1.0 / (light.constant + distance * light.linear + distance * distance * light.quadratic);

    vec3 ambient = attenuation * Mambient * light.lAmbient;

    vec3 lightDir = normalize(light.lightPos - FragPos);

    //diffuse
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse =attenuation * diff * light.lDiffuse * Mdiffuse;

    //specular
    //vec3 reflectDir = reflect(light.lightDir, normal);

    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 halhplane = normalize(viewDir + lightDir);

    float shininess = gNormal.a;
    float spec = pow(max(dot(halhplane, normal), 0.0), shininess );
    vec3 specular = attenuation * spec * light.lSpecular * Mspecular;

    oColor = vec4(ambient   + diffuse    + specular  , 1.0);

}
