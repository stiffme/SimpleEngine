package com.esipeng.opengl.engine.shader;

import com.esipeng.opengl.engine.base.DrawComponentBase;
import com.esipeng.opengl.engine.base.Mesh;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;

import java.util.HashSet;

import static com.esipeng.opengl.engine.base.Constants.*;
import static org.lwjgl.opengl.GL33.*;

public class GBufferInputRenderer extends DrawComponentBase {

    private static final int AMBIENT_TEX = 1, DIFFUSE_TEX = 2, SPECULAR_TEX = 3;

    private int noNormalMapProgram, normalMapProgram;
    private int fbo, texPosition, texNormal,texAmbient,texAlbedoSpec;


    public GBufferInputRenderer(String name) {
        super(name,
                new HashSet<>(),
                new HashSet<String>(){{
                    add(GBUFFER_POSITION);
                    add(GBUFFER_NORMAL);
                    add(GBUFFER_AMBIENT);
                    add(GBUFFER_ALBEDOSPEC);
                    add(GBUFFER_FBO);
                }});
    }

    @Override
    public boolean init(DrawContextIf context) {
        try {
            noNormalMapProgram = compileAndLinkProgram(
                    "shader/GBufferInputRenderer/vertex_no_normal_map.glsl",
                    "shader/GBufferInputRenderer/fragment_no_normal_map.glsl"
            );
            int mvpLoc = glGetUniformBlockIndex(noNormalMapProgram,"MVP");
            glUniformBlockBinding(noNormalMapProgram, mvpLoc, MVP_BINDING_POINT);

            if(!setTextureId(noNormalMapProgram))
                return false;

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        //high precision for position and normal
        texPosition = createTextureForFrameBuffer(GL_RGB16F, GL_RGB, context);
        texNormal = createTextureForFrameBuffer(GL_RGB16F,GL_RGB, context);

        texAmbient = createTextureForFrameBuffer(GL_RGB,GL_RGB,context);
        //vec4 for albedo spec
        texAlbedoSpec = createTextureForFrameBuffer(GL_RGBA, GL_RGBA, context);

        fbo = getManagedFramebuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

//        layout (location = 0) out vec3 gPosition;
//        layout (location = 1) out vec3 gNormal;
//        layout (location = 2) out vec3 gAmbient;
//        layout (location = 3) out vec4 gAlbedoSpec;

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texPosition, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, texNormal, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, GL_TEXTURE_2D, texAmbient, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT3, GL_TEXTURE_2D, texAlbedoSpec, 0);

        int[] drawBuffers = new int[]{
                GL_COLOR_ATTACHMENT0,
                GL_COLOR_ATTACHMENT1,
                GL_COLOR_ATTACHMENT2,
                GL_COLOR_ATTACHMENT3
        };
        glDrawBuffers(drawBuffers);

        //for depth/stencil
        int rbo = getManagedRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, rbo);
        glRenderbufferStorage(GL_RENDERBUFFER,GL_DEPTH24_STENCIL8,
                context.getScreenWidth(), context.getScreenHeight());
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER,rbo);
        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            return false;
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return true;
    }

    private boolean setTextureId(int program)   {
        if(!setUniform1i(program,"texAmbient", AMBIENT_TEX))
            return false;

        if(!setUniform1i(program,"texDiffuse", DIFFUSE_TEX))
            return false;

        if(!setUniform1i(program,"texSpecular", SPECULAR_TEX))
            return false;

        return true;
    }

    private int createTextureForFrameBuffer(int internalFormat, int format, DrawContextIf context) {
        int tex = getManagedTexture();
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexImage2D(GL_TEXTURE_2D,0,internalFormat,
                context.getScreenWidth(),context.getScreenHeight(),0,format, GL_FLOAT,0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glBindTexture(GL_TEXTURE_2D, 0);
        return tex;
    }

    @Override
    public void beforeDraw(DrawContextIf context) {
        glUseProgram(noNormalMapProgram);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glEnable(GL_CULL_FACE);
        glDisable(GL_STENCIL_TEST);
        glBindFramebuffer(GL_FRAMEBUFFER,fbo);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    @Override
    public void draw(DrawContextIf context) {
        for(DrawableObjectIf drawableObject : context.getCurrentDrawableObject())   {
            for(Mesh mesh : drawableObject.getMeshes()) {

                glActiveTexture(GL_TEXTURE0 + AMBIENT_TEX);
                glBindTexture(GL_TEXTURE_2D, mesh.getAmbient());

                glActiveTexture(GL_TEXTURE0 + DIFFUSE_TEX);
                glBindTexture(GL_TEXTURE_2D, mesh.getDiffuse());

                glActiveTexture(GL_TEXTURE0 + SPECULAR_TEX);
                glBindTexture(GL_TEXTURE_2D, mesh.getSpecular());

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
        //flush datums
//        add(GBUFFER_POSITION);
//        add(GBUFFER_NORMAL);
//        add(GBUFFER_AMBIENT);
//        add(GBUFFER_ALBEDOSPEC);
//        add(GBUFFER_FBO);
        context.updateDatum(GBUFFER_POSITION,texPosition);
        context.updateDatum(GBUFFER_NORMAL,texNormal);
        context.updateDatum(GBUFFER_AMBIENT,texAmbient);
        context.updateDatum(GBUFFER_ALBEDOSPEC,texAlbedoSpec);
        context.updateDatum(GBUFFER_FBO,fbo);


        glUseProgram(0);
        glDisable(GL_DEPTH_TEST);
        glBindFramebuffer(GL_FRAMEBUFFER,0);
    }
}
