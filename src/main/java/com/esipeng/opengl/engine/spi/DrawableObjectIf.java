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

    Matrix4f getModelMatrix();
    Iterable<Mesh> getMeshes();
}
