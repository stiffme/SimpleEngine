package com.esipeng.opengl.engine.base;

import com.esipeng.opengl.engine.spi.DrawableObjectIf;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.esipeng.opengl.engine.base.Constants.VERTEX_MAT_MODEL;
import static org.lwjgl.opengl.GL33.*;

public abstract class DrawableObjectBase
        extends ManagedObject
        implements DrawableObjectIf {
    private class ModelData {
        Vector3f position;
        float scaleX, scaleY, scaleZ;
        float rotateRadian;
        Vector3f rotateAxis;

        ModelData() {
            position = new Vector3f();
            scaleX = scaleY = scaleZ = 1.0f;
            rotateRadian = 0.0f;
            rotateAxis = new Vector3f(1.f, 0.0f,0.0f);
        }
    }

    private Matrix4f modelMat;
    private ModelData[] modelData;
    private int modelVBO;
    private float[] tempBuf = new float[16];

    protected DrawableObjectBase(int instanceNumber)  {
        modelData = new ModelData[instanceNumber];
        for(int i = 0; i < instanceNumber; ++i)
            modelData[i] = new ModelData();

        modelMat = new Matrix4f();
        modelVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, modelVBO);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * 16 * instanceNumber,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        flushAllInstances();
    }

    private void flushAllInstances()    {
        for(int i = 0; i < modelData.length; ++i)   {
            flushOneInstance(i);
        }
    }

    private void flushOneInstance(int instanceId){
        modelMat.identity()
                .translate(modelData[instanceId].position)
                .rotate(modelData[instanceId].rotateRadian, modelData[instanceId].rotateAxis)
                .scale(modelData[instanceId].scaleX,modelData[instanceId].scaleY,modelData[instanceId].scaleZ);
        glBindBuffer(GL_ARRAY_BUFFER, modelVBO);
        glBufferSubData(GL_ARRAY_BUFFER, Float.BYTES * 16 * instanceId, modelMat.get(tempBuf));
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }


    @Override
    public Vector3f getPosition() {
        return getPosition(0);
    }

    @Override
    public void setPosition(float x, float y, float z) {
        setPosition(0, x, y, z);
    }

    @Override
    public void setPosition(Vector3f position) {
        setPosition(0, position);
    }

    @Override
    public void setScale(float xyz) {
        setScale(0, xyz);
    }

    @Override
    public void setScale(float x, float y, float z) {
        setScale(0,x,y,z);
    }

    @Override
    public void setRotate(float radians, Vector3f axis) {
        setRotate(0, radians, axis);
    }

    @Override
    public void setRotate(float radians, float x, float y, float z) {
        setRotate(0,radians,x,y,z);
    }


    @Override
    public void setInstances(int number) {
        ModelData[] newData = new ModelData[number];
        System.arraycopy(modelData, 0, newData, 0, Math.min(number, modelData.length));

        for(int t = modelData.length; t < number; ++t)  {
            newData[t] = new ModelData();
        }

        modelData = newData;
        //re-allocate buffer
        glBindBuffer(GL_ARRAY_BUFFER,modelVBO);
        glBufferData(GL_ARRAY_BUFFER,Float.BYTES * 16 * number, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        flushAllInstances();
    }

    @Override
    public Vector3f getPosition(int instanceId) {
        return modelData[instanceId].position;
    }

    @Override
    public void setPosition(int instanceId, float x, float y, float z) {
        modelData[instanceId].position.set(x, y, z);
        flushOneInstance(instanceId);
    }

    @Override
    public void setPosition(int instanceId, Vector3f position) {
        modelData[instanceId].position.set(position);
        flushOneInstance(instanceId);
    }

    @Override
    public void setScale(int instanceId, float xyz) {
        setScale(instanceId,xyz,xyz,xyz);
        flushOneInstance(instanceId);
    }

    @Override
    public void setScale(int instanceId, float x, float y, float z) {
        modelData[instanceId].scaleX = x;
        modelData[instanceId].scaleY = y;
        modelData[instanceId].scaleZ = z;
        flushOneInstance(instanceId);
    }

    @Override
    public void setRotate(int instanceId, float radians, Vector3f axis) {
        modelData[instanceId].rotateRadian = radians;
        modelData[instanceId].rotateAxis.set(axis).normalize();
        flushOneInstance(instanceId);
    }

    @Override
    public void setRotate(int instanceId, float radians, float x, float y, float z) {
        modelData[instanceId].rotateRadian = radians;
        modelData[instanceId].rotateAxis.set(x, y, z).normalize();
        flushOneInstance(instanceId);
    }

    @Override
    public int getInstances() {
        return modelData.length;
    }

    /**
     * override the VAO, binds the model vertex attrib
     * @return vao which binds the model vertex attrib
     */
    @Override
    protected int getManagedVAO() {
        int vao = super.getManagedVAO();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, modelVBO);

        glVertexAttribPointer(VERTEX_MAT_MODEL + 0, 4, GL_FLOAT,false, Float.BYTES * 16, Float.BYTES * 0);
        glVertexAttribPointer(VERTEX_MAT_MODEL + 1, 4, GL_FLOAT,false, Float.BYTES * 16, Float.BYTES * 4);
        glVertexAttribPointer(VERTEX_MAT_MODEL + 2, 4, GL_FLOAT,false, Float.BYTES * 16, Float.BYTES * 8);
        glVertexAttribPointer(VERTEX_MAT_MODEL + 3, 4, GL_FLOAT,false, Float.BYTES * 16, Float.BYTES * 12);

        glEnableVertexAttribArray(VERTEX_MAT_MODEL + 0);
        glEnableVertexAttribArray(VERTEX_MAT_MODEL + 1);
        glEnableVertexAttribArray(VERTEX_MAT_MODEL + 2);
        glEnableVertexAttribArray(VERTEX_MAT_MODEL + 3);

        glVertexAttribDivisor(VERTEX_MAT_MODEL + 0, 1);
        glVertexAttribDivisor(VERTEX_MAT_MODEL + 1, 1);
        glVertexAttribDivisor(VERTEX_MAT_MODEL + 2, 1);
        glVertexAttribDivisor(VERTEX_MAT_MODEL + 3, 1);

        glBindVertexArray(0);
        return vao;
    }
}
