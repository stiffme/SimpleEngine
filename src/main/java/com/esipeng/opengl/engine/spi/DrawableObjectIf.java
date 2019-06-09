package com.esipeng.opengl.engine.spi;

import com.esipeng.opengl.engine.base.Mesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface DrawableObjectIf extends ReleaseIf {
    Vector3f getPosition();
    void setPosition(float x, float y, float z);
    void setPosition(Vector3f position);

    void setScale(float xyz);
    void setScale(float x, float y, float z);

    void setRotate(float radians, Vector3f axis);
    void setRotate(float radians, float x, float y, float z);

    void setInstances(int number);
    int getInstances();
    Vector3f getPosition(int instanceId);
    void setPosition(int instanceId,float x, float y, float z);
    void setPosition(int instanceId,Vector3f position);

    void setScale(int instanceId,float xyz);
    void setScale(int instanceId,float x, float y, float z);

    void setRotate(int instanceId,float radians, Vector3f axis);
    void setRotate(int instanceId,float radians, float x, float y, float z);

    Iterable<Mesh> getMeshes();
}
