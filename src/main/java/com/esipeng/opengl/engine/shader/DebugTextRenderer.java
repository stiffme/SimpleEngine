package com.esipeng.opengl.engine.shader;

import com.esipeng.opengl.engine.base.DrawComponentBase;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.opengl.GL33.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashSet;

public class DebugTextRenderer extends DrawComponentBase {
    private static final int TEXT_TEXTURE = 3;
    private static final int BITMAP_W = 1024;
    private static final int BITMAP_H = 1024;
    private static final int CODE_START = 32;
    private static final int CODE_LENGTH = 96;
    private static final float FONT_HEIGHT = 64f;
    private static final int LINE_SIZE = 64;

    private int program, fontTexture, vao, vbo, ebo;
    private String fontResource;
    private String debugContent;
    private STBTTBakedChar.Buffer cdata;
    private float[] vertices = new float[16 * LINE_SIZE];

    public DebugTextRenderer(String name, String fontResource)  {
        super(name,new HashSet<>(), new HashSet<>());
        this.fontResource = fontResource;
    }

    public void setDebugContent(String debugContent) {
        this.debugContent = debugContent;
    }

    @Override
    public boolean init(DrawContextIf context) {
        try{
            program = compileAndLinkProgram(
                    "shader/DebugTextRenderer/vertex.glsl",
                    "shader/DebugTextRenderer/fragment.glsl"
            );

            byte[] fontData = loadBinaryFileFromResource(fontResource);
            ByteBuffer fontBuffer = MemoryUtil.memAlloc(fontData.length);
            fontBuffer.put(fontData);
            fontBuffer.flip();
            cdata = STBTTBakedChar.malloc(CODE_LENGTH);
            ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
            stbtt_BakeFontBitmap(fontBuffer,FONT_HEIGHT, bitmap, BITMAP_W,BITMAP_H, CODE_START, cdata);
            MemoryUtil.memFree(fontBuffer);

            fontTexture = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, fontTexture);
            glTexImage2D(GL_TEXTURE_2D,0,GL_RED,BITMAP_W,BITMAP_H,0,
                    GL_RED,GL_UNSIGNED_BYTE,bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glBindTexture(GL_TEXTURE_2D, 0);

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        vao = getManagedVAO();
        vbo = getManagedVBO();
        ebo = getManagedVBO();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * 16, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0,2,GL_FLOAT,false,Float.BYTES * 4, 0L);
        glVertexAttribPointer(1,2,GL_FLOAT,false,Float.BYTES * 4, Float.BYTES * 2);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER,Integer.BYTES * 5, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        setUniform1i(program,"textTexture", TEXT_TEXTURE);
        return true;
    }

    @Override
    public void beforeDraw(DrawContextIf context) {
        glUseProgram(program);
        glActiveTexture(GL_TEXTURE0 + TEXT_TEXTURE);
        glBindTexture(GL_TEXTURE_2D, fontTexture);
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_STENCIL_TEST);
        glDisable(GL_CULL_FACE);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void draw(DrawContextIf context) {
        try(MemoryStack stack = MemoryStack.stackPush())    {
            FloatBuffer x = stack.floats(-context.getScreenWidth());
            FloatBuffer y = stack.floats(-context.getScreenHeight() + FONT_HEIGHT * 1.5f);
            STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);

            for(int i = 0; i < debugContent.length(); ++i)  {
                int c = debugContent.charAt(i);
                if(c >= CODE_START && c < CODE_START + CODE_LENGTH)  {
                    stbtt_GetBakedQuad(cdata,BITMAP_W,BITMAP_H,c - 32,x,y,q,true);
                    vertices[0] = q.x0() / context.getScreenWidth();
                    vertices[1] = -q.y0() / context.getScreenHeight();
                    vertices[2] = q.s0();
                    vertices[3] = q.t0();

                    vertices[4] = q.x1()/ context.getScreenWidth();
                    vertices[5] = -q.y0()/ context.getScreenHeight();
                    vertices[6] = q.s1();
                    vertices[7] = q.t0();


                    vertices[8] = q.x1()/ context.getScreenWidth();
                    vertices[9] = -q.y1()/ context.getScreenHeight();
                    vertices[10] = q.s1();
                    vertices[11] = q.t1();

                    vertices[12] = q.x0()/ context.getScreenWidth();
                    vertices[13] = -q.y1()/ context.getScreenHeight();
                    vertices[14] = q.s0();
                    vertices[15] = q.t1();

                    glBufferData(GL_ARRAY_BUFFER,vertices,GL_DYNAMIC_DRAW);
                    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
                }
            }
        }

    }

    @Override
    public void afterDraw(DrawContextIf context) {
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glUseProgram(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    public void release() {
        super.release();
        cdata.free();

    }
}
