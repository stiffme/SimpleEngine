package com.esipeng.opengl.engine.base.light;

import com.esipeng.opengl.engine.base.Constants;
import com.esipeng.opengl.engine.base.ManagedObject;
import com.esipeng.opengl.engine.base.UBOManager;

public class AbstractLight extends ManagedObject {
//    uniform Light {
//        vec3 lightPos;
//        float quadratic;
//        vec3 lightDir;
//        float constant;
//        vec3 lAmbient;
//        float linear;
//        vec3 lDiffuse;
//        vec3 lSpecular;
//    } light;

    protected UBOManager uboManager ;
    private int ubo;

    protected AbstractLight()   {
        ubo = getManagedVBO();
        uboManager = new UBOManager(Constants.LIGHT_UBO_MANAGER, ubo);
    }

    public int getUbo() {
        return ubo;
    }
}
