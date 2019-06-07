package com.esipeng.opengl.engine.base;

import com.esipeng.opengl.engine.spi.DrawableObjectIf;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class DrawableObjectBase
        extends ManagedObject
        implements DrawableObjectIf {
    protected Vector3f position;
    protected float scaleX, scaleY, scaleZ;
    protected float rotateRadian;
    protected Vector3f rotateAxis;
    protected Matrix4f modelMat;

    protected DrawableObjectBase()  {
        position = new Vector3f();
        scaleX = scaleY = scaleZ = 1.0f;
        rotateRadian = 0.0f;
        rotateAxis = new Vector3f(1.f, 0.0f,0.0f);
        modelMat = new Matrix4f();
    }

    @Override
    public Vector3f getPosition() {
        return position;
    }

    @Override
    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    @Override
    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    @Override
    public void setScale(float xyz) {
        scaleX = scaleY = scaleZ = xyz;
    }

    @Override
    public void setScale(float x, float y, float z) {
        scaleX = x;
        scaleY = y;
        scaleZ = z;
    }

    @Override
    public void setRotate(float radians, Vector3f axis) {
        rotateRadian = radians;
        rotateAxis.set(axis).normalize();
    }

    @Override
    public void setRotate(float radians, float x, float y, float z) {
        rotateRadian = radians;
        rotateAxis.x = x;
        rotateAxis.y = y;
        rotateAxis.z = z;
        rotateAxis.normalize();
    }

    @Override
    public Matrix4f getModelMatrix() {
        modelMat.identity().translate(position).rotate(rotateRadian, rotateAxis).scale(scaleX,scaleY,scaleZ);
        return modelMat;
    }

//    @Override
//    public void release() {
//        super.release();
//        for(Mesh mesh : getMeshes())
//            mesh.release();
//    }
}
