package com.esipeng.opengl.engine.shader;

import com.esipeng.opengl.engine.base.DrawComponentBase;
import com.esipeng.opengl.engine.base.Mesh;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;

import java.util.HashSet;

import static com.esipeng.opengl.engine.base.Constants.MVP_BINDING_POINT;
import static org.lwjgl.opengl.GL33.*;


public class PlainInputRenderer extends DrawComponentBase {

    private static final int AMBIENT_TEX = 8;
    private static final int DIFFUSE_TEX = 9;

    private String output;
    private int outputTexture, fbo;
    private int program;

    public PlainInputRenderer(String name, String output)   {
        super(name, new HashSet<>(), new HashSet<String>(){{add(output);}});
        this.output = output;
    }

    @Override
    public boolean init(DrawContextIf context) {
        try{
            program = compileAndLinkProgram(
                    "shader/PlainInputRenderer/vertex.glsl",
                    "shader/PlainInputRenderer/fragment.glsl"
            );
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        //MVP
        int mvpLoc = glGetUniformBlockIndex(program,"MVP");
        if(mvpLoc == -1)
            return false;
        glUniformBlockBinding(program, mvpLoc, MVP_BINDING_POINT);

        if(!setUniform1i(program, "ambient", AMBIENT_TEX))
            return false;

        if(!setUniform1i(program, "diffuse", DIFFUSE_TEX))
            return false;

        //create fbo texture
        outputTexture = getManagedTexture();
        glBindTexture(GL_TEXTURE_2D, outputTexture);
        glTexImage2D(GL_TEXTURE_2D,0,GL_RGB,
                context.getScreenWidth(), context.getScreenHeight(),
                0,GL_RGB,GL_UNSIGNED_BYTE,0L);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        //create render buffer
        int rbo = getManagedRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER,rbo);
        glRenderbufferStorage(GL_RENDERBUFFER,GL_DEPTH24_STENCIL8,
                context.getScreenWidth(),context.getScreenHeight());
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        //create FBO
        fbo = getManagedFramebuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D,
                outputTexture, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo);
        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            return false;

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return true;
    }

    @Override
    public void beforeDraw(DrawContextIf context) {
        glUseProgram(program);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_STENCIL_TEST);
    }

    @Override
    public void draw(DrawContextIf context) {
        for(DrawableObjectIf drawableObject : context.getCurrentDrawableObject())   {
            for(Mesh mesh : drawableObject.getMeshes()) {
                glActiveTexture(GL_TEXTURE0 + AMBIENT_TEX);
                glBindTexture(GL_TEXTURE_2D, mesh.getAmbient());

                glActiveTexture(GL_TEXTURE0 + DIFFUSE_TEX);
                glBindTexture(GL_TEXTURE_2D, mesh.getDiffuse());

                glBindVertexArray(mesh.getVao());
                if(mesh.isUseIndices())
                    glDrawElementsInstanced(GL_TRIANGLES, mesh.getVerticesNumber(), GL_UNSIGNED_INT, 0L,drawableObject.getInstances());
                else
                    glDrawArraysInstanced(GL_TRIANGLES, 0, mesh.getVerticesNumber(), drawableObject.getInstances());
            }
        }
    }

    @Override
    public void afterDraw(DrawContextIf context) {
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glUseProgram(0);
        context.updateDatum(output, outputTexture);
    }
}
