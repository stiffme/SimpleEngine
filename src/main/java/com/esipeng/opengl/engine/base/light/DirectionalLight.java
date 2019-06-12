package com.esipeng.opengl.engine.base.light;

import org.joml.Vector3f;

import static com.esipeng.opengl.engine.base.Constants.*;

public class DirectionalLight extends AbstractLight {
    public DirectionalLight(
            float ambientR, float ambientG, float ambientB,
            float diffuseR, float diffuseG, float diffuseB,
            float specularR, float specularG, float specularB,
            float dirX, float dirY, float dirZ
    )   {
        uboManager.setValue(UNIFORM_LIGHT_AMBIENT, ambientR, ambientG, ambientB);
        uboManager.setValue(UNIFORM_LIGHT_DIFFUSE, diffuseR, diffuseG, diffuseB);
        uboManager.setValue(UNIFORM_LIGHT_SPECULAR, specularR, specularG, specularB);
        Vector3f dir = new Vector3f(dirX, dirY, dirZ).normalize();
        uboManager.setValue(UNIFORM_LIGHT_DIR, dir);
    }

    public DirectionalLight(
            Vector3f ambient,
            Vector3f diffuse,
            Vector3f specular,
            Vector3f dir
    )   {
        uboManager.setValue(UNIFORM_LIGHT_AMBIENT, ambient);
        uboManager.setValue(UNIFORM_LIGHT_DIFFUSE, diffuse);
        uboManager.setValue(UNIFORM_LIGHT_SPECULAR, specular);
        uboManager.setValue(UNIFORM_LIGHT_DIR, dir.normalize());
    }
}
