package com.esipeng.opengl.engine.shader;

import com.esipeng.opengl.engine.base.DrawComponentBase;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

import static org.lwjgl.opengl.GL33.*;

public class OutputScreen extends DrawComponentBase {
    private static Logger logger = LoggerFactory.getLogger(OutputScreen.class);
    private static final int SCREEN_TEXTURE = 5;
    private String inputTexture;
    private int program, vao;

    public OutputScreen(String name, String inputTexture)   {
        super(name, new HashSet<String>(){{add(inputTexture);}}, new HashSet<>());
        this.inputTexture = inputTexture;
    }

    @Override
    public boolean init(DrawContextIf context) {
        try{
            program = compileAndLinkProgram(
                    "shader/OutputScreen/vertex.glsl",
                    "shader/OutputScreen/fragment.glsl"
            );
            if(program == 0)
                return false;

            if(!setUniform1i(program,"screenTexture",SCREEN_TEXTURE))
                return false;

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        float[] quadVertices = { // vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates.
                // positions   // texCoords
                -1.0f,  1.0f,  0.0f, 1.0f,
                -1.0f, -1.0f,  0.0f, 0.0f,
                1.0f, -1.0f,  1.0f, 0.0f,

                -1.0f,  1.0f,  0.0f, 1.0f,
                1.0f, -1.0f,  1.0f, 0.0f,
                1.0f,  1.0f,  1.0f, 1.0f
        };

        int vbo = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
        vao = getManagedVAO();
        glBindVertexArray(vao);
        glVertexAttribPointer(0,2,GL_FLOAT,false,Float.BYTES * 4, 0L);
        glVertexAttribPointer(1,2,GL_FLOAT,false,Float.BYTES * 4, Float.BYTES * 2);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        return true;
    }

    @Override
    public void beforeDraw(DrawContextIf context) {
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_STENCIL_TEST);

        glBindVertexArray(vao);
        glUseProgram(program);
        glActiveTexture(GL_TEXTURE0 + SCREEN_TEXTURE);
        int inputTextureId = context.retrieveDatum(this.inputTexture);
        glBindTexture(GL_TEXTURE_2D, inputTextureId);

        //make sure it outputs to the screen
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void draw(DrawContextIf context) {
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    @Override
    public void afterDraw(DrawContextIf context) {
        glBindVertexArray(0);
        glUseProgram(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glActiveTexture(GL_TEXTURE0);
    }
}
