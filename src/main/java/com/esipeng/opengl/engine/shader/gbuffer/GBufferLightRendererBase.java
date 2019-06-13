package com.esipeng.opengl.engine.shader.gbuffer;

import com.esipeng.opengl.engine.base.DrawComponentBase;
import com.esipeng.opengl.engine.base.light.AbstractLight;
import com.esipeng.opengl.engine.spi.DrawContextIf;

import java.util.HashSet;

import static com.esipeng.opengl.engine.base.Constants.*;
import static org.lwjgl.opengl.GL33.*;

public abstract class GBufferLightRendererBase extends DrawComponentBase {
    private static final int TEX_GPOSITION = 10;
    private static final int TEX_GNORMAL = 11;
    private static final int TEX_GAMBIENT = 12;
    private static final int TEX_GALBEDOSPEC = 13;

    private int vao;
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
        program = compileProgram();
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

        float[] quadVertices = { // vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates.
                // positions   // texCoords
                -1.0f,  1.0f,  0.0f, 1.0f,
                -1.0f, -1.0f,  0.0f, 0.0f,
                1.0f, -1.0f,  1.0f, 0.0f,

                -1.0f,  1.0f,  0.0f, 1.0f,
                1.0f, -1.0f,  1.0f, 0.0f,
                1.0f,  1.0f,  1.0f, 1.0f
        };

        vao = getManagedVAO();
        int vbo = getManagedVBO();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER,quadVertices,GL_STATIC_DRAW);
        glVertexAttribPointer(0,2,GL_FLOAT,false,Float.BYTES * 4, 0);
        glVertexAttribPointer(1,2,GL_FLOAT,false,Float.BYTES * 4, Float.BYTES * 2);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return true;
    }

    protected abstract int compileProgram();

    @Override
    public void beforeDraw(DrawContextIf context) {
        glUseProgram(program);
        glBindFramebuffer(GL_FRAMEBUFFER, context.retrieveDatum(GBUFFER_COMPOSITOR_FBO));
        glBindVertexArray(vao);
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

    @Override
    public void draw(DrawContextIf context) {
        Iterable<AbstractLight> lights = getLights(context);
        for(AbstractLight light : lights)   {
            glBindBufferBase(GL_UNIFORM_BUFFER, LIGHT_BINDING_POINT, light.getUbo());
            glDrawArrays(GL_TRIANGLES,0,6);

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
}
