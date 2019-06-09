package com.esipeng.opengl.engine.shader;

import com.esipeng.opengl.engine.base.DrawComponentBase;
import com.esipeng.opengl.engine.spi.DrawContextIf;

import java.util.HashSet;
import java.util.List;

import static org.lwjgl.opengl.GL33.*;

public class DebugWindowsRenderer extends DrawComponentBase {
    private static final int DEBUG_TEXTURE_ID = 5;
    private static final int MAX_NUMBER = 4;

    private List<String> inputs;
    private int program, vao, vbo;
    //private float alpha = 1.0f;
    private float[] quards = new float[8];

    public DebugWindowsRenderer(String name, List<String> inputDatums)  {
        super(name, new HashSet<String>(){{addAll(inputDatums);}}, new HashSet<>());
        inputs = inputDatums;
    }

    @Override
    public boolean init(DrawContextIf context) {
        try {
            program = compileAndLinkProgram(
                    "shader/DebugWindowsRenderer/vertex.glsl",
                    "shader/DebugWindowsRenderer/fragment.glsl"
            );
            if(!setUniform1i(program, "debugTexture", DEBUG_TEXTURE_ID))
                return false;

            if(!setUniform1f(program,"debugAlpha",1.0f))
                return false;


        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

        float[] texcoords = new float[]   {
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
        };

        //VBO layout
        // 4 * vec2 POS --> 4 * vec2 texcoords
        vbo = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER,Float.BYTES * 16, GL_DYNAMIC_DRAW);
        glBufferSubData(GL_ARRAY_BUFFER, Float.BYTES * 8, texcoords);

        vao = getManagedVAO();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0,2,GL_FLOAT,false,0,0);
        glVertexAttribPointer(1,2,GL_FLOAT,false,0,Float.BYTES * 8);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        return true;
    }

    public void setAlpha(float alpha)   {
        setUniform1f(program,"debugAlpha",alpha);
    }

    @Override
    public void beforeDraw(DrawContextIf context) {
        glUseProgram(program);
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_STENCIL_TEST);
        glActiveTexture(GL_TEXTURE0 + DEBUG_TEXTURE_ID);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void draw(DrawContextIf context) {
        int index = 0;
        float gap = 2.0f / MAX_NUMBER;

        for(String datum : inputs)  {
            int input = context.retrieveDatum(datum);
            glBindTexture(GL_TEXTURE_2D, input);

            int line = index / MAX_NUMBER;
            int col = index % MAX_NUMBER;

            float startx = col * gap - 1.0f;
            float starty = line * gap - 1.0f;

            quards[0] = startx;
            quards[1] = starty;

            quards[2] = startx + gap;
            quards[3] = starty;

            quards[4] = startx + gap;
            quards[5] = starty + gap;

            quards[6] = startx;
            quards[7] = starty + gap;

            glBufferSubData(GL_ARRAY_BUFFER, 0L,quards);

            glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
            ++index;
        }
    }

    @Override
    public void afterDraw(DrawContextIf context) {
        glUseProgram(0);
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glBindTexture(GL_TEXTURE_2D, 0);
        glActiveTexture(GL_TEXTURE0);
        glDisable(GL_BLEND);
    }
}
