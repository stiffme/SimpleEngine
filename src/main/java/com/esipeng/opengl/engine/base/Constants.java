package com.esipeng.opengl.engine.base;

public class Constants {
    public static final int MVP_BINDING_POINT = 55;

    //vertex location
    public static final int VERTEX_MAT_MODEL = 0;
    public static final int VERTEX_VEC3_POS = 4;
    public static final int VERTEX_VEC2_TEXCOORD = 5;
    public static final int VERTEX_VEC3_NORMAL = 6;
    public static final int VERTEX_VEC3_TANGENT = 7;
    public static final int VERTEX_VEC3_BITANGENT = 8;

//    layout (location = 0) out vec3 gPosition;
//    layout (location = 1) out vec3 gNormal;
//    layout (location = 2) out vec3 gAmbient;
//    layout (location = 3) out vec4 gAlbedoSpec;
    public static final String GBUFFER_POSITION = "GBUFFER_POSITION";
    public static final String GBUFFER_NORMAL = "GBUFFER_NORMAL";
    public static final String GBUFFER_AMBIENT = "GBUFFER_AMBIENT";
    public static final String GBUFFER_ALBEDOSPEC = "GBUFFER_ALBEDOSPEC";
    public static final String GBUFFER_FBO = "GBUFFER_FBO";
}
