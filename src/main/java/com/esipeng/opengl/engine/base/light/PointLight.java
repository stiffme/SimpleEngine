package com.esipeng.opengl.engine.base.light;

import org.joml.Vector3f;

import static com.esipeng.opengl.engine.base.Constants.*;

public class PointLight extends AbstractLight {

    private Vector3f pos;
    private float maxIntense = 0;
    private float constant, linear, quadratic;

    public PointLight(
            float ambientR, float ambientG, float ambientB,
            float diffuseR, float diffuseG, float diffuseB,
            float specularR, float specularG, float specularB,
            float posX, float posY, float posZ,
            float constant, float linear, float quadratic
    )   {

        this(new Vector3f(ambientR, ambientG, ambientB),
                new Vector3f(diffuseR,diffuseG,diffuseB),
                new Vector3f(specularR,specularG,specularB),
                new Vector3f(posX,posY,posZ),
                constant,linear,quadratic);
    }

    public PointLight(
            Vector3f ambient,
            Vector3f diffuse,
            Vector3f specular,
            Vector3f pos,
            float constant, float linear, float quadratic
    )   {
        uboManager.setValue(UNIFORM_LIGHT_AMBIENT, ambient);
        uboManager.setValue(UNIFORM_LIGHT_DIFFUSE, diffuse);
        uboManager.setValue(UNIFORM_LIGHT_SPECULAR, specular);
        uboManager.setValue(UNIFORM_LIGHT_POS, pos);
        uboManager.setValue(UNIFORM_LIGHT_CONSTANT, constant);
        uboManager.setValue(UNIFORM_LIGHT_LINEAR, linear);
        uboManager.setValue(UNIFORM_LIGHT_QUADRATIC, quadratic);
        this.pos = new Vector3f().set(pos);
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;

        maxIntense = ambient.get(ambient.maxComponent());
        maxIntense = Math.max(maxIntense, diffuse.get(diffuse.maxComponent()));
        maxIntense = Math.max(maxIntense, specular.get(specular.maxComponent()));
    }

    public Vector3f getPos()    {
        return pos;
    }

    public float getRadius()    {
        float delta = (float)Math.sqrt(linear * linear - 4 * quadratic * (constant - 256 * maxIntense));
        float ret = (-linear + delta) / (2 * quadratic);
        return ret;
        //return 2;
    }
}
