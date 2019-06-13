package com.esipeng.opengl.engine.shader.gbuffer;

import com.esipeng.opengl.engine.base.DrawComponentBase;
import com.esipeng.opengl.engine.base.Mesh;
import com.esipeng.opengl.engine.base.light.AbstractLight;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;

import java.util.HashSet;

import static com.esipeng.opengl.engine.base.Constants.*;
import static org.lwjgl.opengl.GL33.*;

public abstract class GBufferLightRendererBase extends DrawComponentBase {
    private static final int TEX_GPOSITION = 10;
    private static final int TEX_GNORMAL = 11;
    private static final int TEX_GAMBIENT = 12;
    private static final int TEX_GALBEDOSPEC = 13;

    protected DrawableObjectIf drawableObject;
    private int program;

    protected GBufferLightRendererBase(String name)   {
        super(name,
        new HashSet<String>(){{
            add(GBUFFER_POSITION);
            add(GBUFFER_NORMAL);
            add(GBUFFER_AMBIENT);
            add(GBUFFER_ALBEDOSPEC);
            add(GBUFFER_COMPOSITOR_FBO);
        }}, new HashSet<>());
    }

    @Override
    public boolean init(DrawContextIf context){
        program = compileProgram(context);
        if(program == 0)
            return false;

        //GBuffer Texture ID
        if(!setUniform1i(program,"GPosition", TEX_GPOSITION))
            return false;
        if(!setUniform1i(program,"GNormal", TEX_GNORMAL))
            return false;
        if(!setUniform1i(program,"GAmbient", TEX_GAMBIENT))
            return false;
        if(!setUniform1i(program,"GAlbedoSpec", TEX_GALBEDOSPEC))
            return false;

        //Light uniform block
        int lightBlockLoc = glGetUniformBlockIndex(program,"Light");
        if(lightBlockLoc == -1)
            return false;
        glUniformBlockBinding(program, lightBlockLoc, LIGHT_BINDING_POINT);

        int viewPosBlockLoc = glGetUniformBlockIndex(program, "ViewPos");
        if(viewPosBlockLoc == -1)
            return false;
        glUniformBlockBinding(program, viewPosBlockLoc, VIEW_POS_BINDING_POINT);
        this.drawableObject = buildDrawableObject();

        return true;
    }

    protected abstract DrawableObjectIf buildDrawableObject()    ;

    protected abstract int compileProgram(DrawContextIf context);

    @Override
    public void beforeDraw(DrawContextIf context) {
        glUseProgram(program);
        glBindFramebuffer(GL_FRAMEBUFFER, context.retrieveDatum(GBUFFER_COMPOSITOR_FBO));
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_STENCIL_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        glActiveTexture(GL_TEXTURE0 + TEX_GPOSITION);
        glBindTexture(GL_TEXTURE_2D, context.retrieveDatum(GBUFFER_POSITION));

        glActiveTexture(GL_TEXTURE0 + TEX_GNORMAL);
        glBindTexture(GL_TEXTURE_2D, context.retrieveDatum(GBUFFER_NORMAL));

        glActiveTexture(GL_TEXTURE0 + TEX_GAMBIENT);
        glBindTexture(GL_TEXTURE_2D, context.retrieveDatum(GBUFFER_AMBIENT));

        glActiveTexture(GL_TEXTURE0 + TEX_GALBEDOSPEC);
        glBindTexture(GL_TEXTURE_2D, context.retrieveDatum(GBUFFER_ALBEDOSPEC));
    }

    protected abstract void handleOneLight(AbstractLight light, DrawableObjectIf drawableObject);

    @Override
    public void draw(DrawContextIf context) {
        Iterable<AbstractLight> lights = getLights(context);
        for(AbstractLight light : lights)   {
            glBindBufferBase(GL_UNIFORM_BUFFER, LIGHT_BINDING_POINT, light.getUbo());
            handleOneLight(light, drawableObject);
            for(Mesh mesh : drawableObject.getMeshes()) {
                glBindVertexArray(mesh.getVao());
                if(!mesh.isUseIndices())
                    glDrawArrays(GL_TRIANGLES,0,6);
                else
                    glDrawElements(GL_TRIANGLES,mesh.getVerticesNumber(),GL_UNSIGNED_INT,0);
            }

        }
    }

    protected abstract Iterable<AbstractLight> getLights(DrawContextIf context);

    @Override
    public void afterDraw(DrawContextIf context) {
        glUseProgram(0);
        glBindVertexArray(0);

        glActiveTexture(GL_TEXTURE0 + TEX_GPOSITION);
        glBindTexture(GL_TEXTURE_2D, 0);

        glActiveTexture(GL_TEXTURE0 + TEX_GNORMAL);
        glBindTexture(GL_TEXTURE_2D, 0);

        glActiveTexture(GL_TEXTURE0 + TEX_GAMBIENT);
        glBindTexture(GL_TEXTURE_2D, 0);

        glActiveTexture(GL_TEXTURE0 + TEX_GALBEDOSPEC);
        glBindTexture(GL_TEXTURE_2D, 0);

        glActiveTexture(GL_TEXTURE0);

        glDisable(GL_BLEND);
        glBindFramebuffer(GL_FRAMEBUFFER,0);
    }

    @Override
    public void release() {
        super.release();
        if(drawableObject != null)  {
            drawableObject.release();
            drawableObject = null;
        }

    }
}
