package com.esipeng.opengl.engine.shader.gbuffer;

import com.esipeng.opengl.engine.base.light.AbstractLight;
import com.esipeng.opengl.engine.base.light.PointLight;
import com.esipeng.opengl.engine.importer.ModelImporter;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;

import static com.esipeng.opengl.engine.base.Constants.MVP_BINDING_POINT;
import static org.lwjgl.opengl.GL33.*;

public class GBufferPointLightRenderer extends GBufferLightRendererBase {

    public GBufferPointLightRenderer(String name) {
        super(name);
    }

    @Override
    protected DrawableObjectIf buildDrawableObject() {


        ModelImporter sphereModel = new ModelImporter();
        sphereModel.loadFromResource("model/sphere/sphere.obj");

        return sphereModel;
    }

    @Override
    protected int compileProgram(DrawContextIf context) {
        try {
            int program = compileAndLinkProgram(
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
    }

    @Override
    protected void handleOneLight(AbstractLight light, DrawableObjectIf drawableObject) {
        PointLight pointLight = (PointLight)light;
        drawableObject.setPosition(pointLight.getPos());
        drawableObject.setScale(pointLight.getRadius());
    }

    @Override
    protected Iterable<AbstractLight> getLights(DrawContextIf context) {
        return context.getWorld().getPointLights();
    }
}
