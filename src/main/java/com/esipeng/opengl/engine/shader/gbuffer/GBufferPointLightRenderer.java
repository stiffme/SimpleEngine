package com.esipeng.opengl.engine.shader.gbuffer;

import com.esipeng.opengl.engine.base.Mesh;
import com.esipeng.opengl.engine.base.light.AbstractLight;
import com.esipeng.opengl.engine.base.light.PointLight;
import com.esipeng.opengl.engine.importer.ModelImporter;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;

import static com.esipeng.opengl.engine.base.Constants.LIGHT_BINDING_POINT;
import static com.esipeng.opengl.engine.base.Constants.MVP_BINDING_POINT;
import static org.lwjgl.opengl.GL33.*;

public class GBufferPointLightRenderer extends GBufferLightRendererBase {

    private boolean debug = false;
    public GBufferPointLightRenderer(String name) {
        super(name);
    }

    public GBufferPointLightRenderer(String name, boolean debug)    {
        super(name);
        this.debug = debug;
    }
    private int dummyShader,program;
    @Override
    protected DrawableObjectIf buildDrawableObject() {


        ModelImporter sphereModel = new ModelImporter();
        sphereModel.loadFromResource("model/sphere/sphere.obj");

        return sphereModel;
    }

    @Override
    protected int compileProgram(DrawContextIf context) {
        try {
            program = compileAndLinkProgram(
                    "shader/GBufferLightRenderer/PointLight/vertex.glsl",
                    "shader/GBufferLightRenderer/PointLight/fragment.glsl"
            );
            if(!setUniform2f(program,"screenSize", context.getScreenWidth(), context.getScreenHeight()))
                return 0;

            //bind MVP
            int mvpLoc = glGetUniformBlockIndex(program,"MVP");
            if(mvpLoc == -1)
                return 0;
            glUniformBlockBinding(program,mvpLoc, MVP_BINDING_POINT);

            dummyShader = compileAndLinkProgram(
                    "shader/GBufferLightRenderer/PointLight/vertex.glsl",
                    "shader/GBufferLightRenderer/PointLight/fragmentDummy.glsl");
            mvpLoc = glGetUniformBlockIndex(dummyShader,"MVP");
            if(mvpLoc == -1)
                return 0;
            glUniformBlockBinding(dummyShader,mvpLoc, MVP_BINDING_POINT);

            return program;
        } catch (Exception e)   {
            e.printStackTrace();
            return 0;
        }

    }

    @Override
    public void beforeDraw(DrawContextIf context) {
        super.beforeDraw(context);
        glDisable(GL_CULL_FACE);
        glEnable(GL_STENCIL_TEST);
        glDepthMask(false);
    }

    @Override
    protected void handleOneLight(AbstractLight light, DrawableObjectIf drawableObject) {
        PointLight pointLight = (PointLight)light;
        drawableObject.setPosition(pointLight.getPos());
        drawableObject.setScale(pointLight.getRadius());
    }

    @Override
    public void draw(DrawContextIf context) {

        Iterable<AbstractLight> lights = getLights(context);
        for(AbstractLight light : lights)   {
            glClear(GL_STENCIL_BUFFER_BIT);
            handleOneLight(light, drawObject);
            glBindBufferBase(GL_UNIFORM_BUFFER, LIGHT_BINDING_POINT, light.getUbo());
            //1st pass
            glDrawBuffer(GL_NONE);
            glUseProgram(dummyShader);
            glEnable(GL_DEPTH_TEST);

            glStencilFunc(GL_ALWAYS, 0, 0);
            glStencilOpSeparate(GL_BACK, GL_KEEP, GL_INCR_WRAP, GL_KEEP);
            glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_DECR_WRAP, GL_KEEP);
            renderObject(drawObject);

            //2nd pass
            glDrawBuffer(GL_COLOR_ATTACHMENT0);
            glStencilOp(GL_KEEP,GL_KEEP,GL_KEEP);
            glDisable(GL_DEPTH_TEST);

            glEnable(GL_CULL_FACE);
            glCullFace(GL_FRONT);

            glUseProgram(program);
            glStencilFunc(GL_NOTEQUAL,0x0,0xFF);
            renderObject(drawObject);

            glCullFace(GL_BACK);
            glDisable(GL_CULL_FACE);

            if(debug)   {
                glUseProgram(dummyShader);
                glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
                glStencilFunc(GL_ALWAYS,0,0xFF);
                //glEnable(GL_CULL_FACE);
                renderObject(drawObject);
                //glDisable(GL_CULL_FACE);
                glBlendFunc(GL_ONE, GL_ONE);
            }

        }
    }

    @Override
    public void afterDraw(DrawContextIf context) {
        super.afterDraw(context);
        glDisable(GL_STENCIL_TEST);
        glDepthMask(true);
    }

    private void renderObject(DrawableObjectIf drawableObject)  {
        for(Mesh mesh : drawableObject.getMeshes()) {
            glBindVertexArray(mesh.getVao());
            if(mesh.isUseIndices())
                glDrawElementsInstanced(GL_TRIANGLES, mesh.getVerticesNumber(), GL_UNSIGNED_INT, 0L,drawableObject.getInstances());
            else
                glDrawArraysInstanced(GL_TRIANGLES, 0, mesh.getVerticesNumber(), drawableObject.getInstances());
        }
    }


    private void renderObjectLine(DrawableObjectIf drawableObject)  {
        for(Mesh mesh : drawableObject.getMeshes()) {
            glBindVertexArray(mesh.getVao());
            if(mesh.isUseIndices())
                glDrawElementsInstanced(GL_LINE_STRIP, mesh.getVerticesNumber(), GL_UNSIGNED_INT, 0L,drawableObject.getInstances());
            else
                glDrawArraysInstanced(GL_LINE_STRIP, 0, mesh.getVerticesNumber(), drawableObject.getInstances());
        }
    }

    @Override
    protected Iterable<AbstractLight> getLights(DrawContextIf context) {
        return context.getWorld().getPointLights();
    }
}
