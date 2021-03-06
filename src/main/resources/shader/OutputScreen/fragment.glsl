#version 330 core
out vec4 FragColor;
uniform sampler2D screenTexture;
in vec2 texCoord;

void main() {
    FragColor = texture(screenTexture,texCoord);
    FragColor = vec4(FragColor.rgb, 1.0);
}
