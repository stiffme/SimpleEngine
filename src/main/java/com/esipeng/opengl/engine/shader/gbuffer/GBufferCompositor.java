package com.esipeng.opengl.engine.shader.gbuffer;

import com.esipeng.opengl.engine.base.DrawComponentBase;
import com.esipeng.opengl.engine.spi.DrawContextIf;

import java.util.HashSet;

import static com.esipeng.opengl.engine.base.Constants.GBUFFER_COMPOSITOR_FBO;
import static com.esipeng.opengl.engine.base.Constants.GBUFFER_COMPOSITOR_TEXTURE;
import static org.lwjgl.opengl.GL33.*;

public class GBufferCompositor extends DrawComponentBase {
    private int compositor;
    private int tex;
    public GBufferCompositor(String name ) {
        super(name, new HashSet<>(),
                new HashSet<String>(){{
                    add(GBUFFER_COMPOSITOR_FBO);
                    add(GBUFFER_COMPOSITOR_TEXTURE);
        }});
    }

    @Override
    public boolean init(DrawContextIf context) {
        compositor = getManagedFramebuffer();
        tex = getManagedTexture();
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexImage2D(GL_TEXTURE_2D,0,GL_RGB,
                context.getScreenWidth(), context.getScreenHeight(),0,GL_RGB,GL_UNSIGNED_BYTE,0);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, compositor);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D, tex, 0);
        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            return false;
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    @Override
    public void beforeDraw(DrawContextIf context) {
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_STENCIL_TEST);
        glClearColor(0,0,0,1);
        glBindFramebuffer(GL_FRAMEBUFFER, compositor);
    }

    @Override
    public void draw(DrawContextIf context) {
        glClear(GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void afterDraw(DrawContextIf context) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        context.updateDatum(GBUFFER_COMPOSITOR_FBO, compositor);
        context.updateDatum(GBUFFER_COMPOSITOR_TEXTURE,tex);
    }
}
