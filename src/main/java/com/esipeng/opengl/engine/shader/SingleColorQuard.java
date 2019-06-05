package com.esipeng.opengl.engine.shader;

import com.esipeng.opengl.engine.base.DrawComponentBase;
import com.esipeng.opengl.engine.spi.DrawComponentIf;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import org.joml.Vector3f;

import java.util.HashSet;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.util.Set;

public class SingleColorQuard extends DrawComponentBase {
    private Vector3f color;
    private int vao, program;
    private int fbo, fboTexture;
    private String outputDatum;
    public SingleColorQuard(String name, String outputDatum, float r, float g, float b) {
        super(name,
                new HashSet<String>(),
                new HashSet<String>(){{add(outputDatum);}});

        color = new Vector3f(r,g,b);
        this.outputDatum = outputDatum;
    }

    @Override
    public boolean init(DrawContextIf context) {
        try{
            program = compileAndLinkProgram(
                    "shader/SingleColorQuard/vertex.glsl",
                    "shader/SingleColorQuard/fragment.glsl"
            );
            if(program == 0)
                return false;

            if(!setUniform3f(program,"color", color))
                return false;
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        float[] quadVertices = {
                // positions
                -1.0f,  1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,

                -1.0f,  1.0f,
                1.0f, -1.0f,
                1.0f,  1.0f,
        };

        int vbo = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
        vao = getManagedVAO();
        glBindVertexArray(vao);
        glVertexAttribPointer(0,2,GL_FLOAT,false,0,0L);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //create framebuffer texture
        fboTexture = getManagedTexture();
        glBindTexture(GL_TEXTURE_2D, fboTexture);
        glTexImage2D(GL_TEXTURE_2D,0,GL_RGB,
                context.getScreenWidth(),context.getScreenHeight(),0,
                GL_RGB,GL_UNSIGNED_BYTE,NULL);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);

        //create fbo
        fbo = getManagedFramebuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fboTexture, 0);
        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            return false;
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    @Override
    public void beforeDraw(DrawContextIf context) {
        glBindVertexArray(vao);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glUseProgram(program);
    }

    @Override
    public void draw(DrawContextIf context) {
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    @Override
    public void afterDraw(DrawContextIf context) {
        glBindVertexArray(0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glUseProgram(0);
        context.updateDatum(outputDatum, fboTexture);
    }
}
