package com.esipeng.opengl.engine.shader;

import com.esipeng.opengl.engine.base.DrawComponentBase;
import com.esipeng.opengl.engine.spi.DrawContextIf;

import java.util.HashSet;

import static org.lwjgl.opengl.GL33.*;

public class OutputScreenFBO extends DrawComponentBase {
    private String fboDatum;
    public OutputScreenFBO(String name, String fboDatum) {
        super(name, new HashSet<String>()
        {{add(fboDatum);}}, new HashSet<>());
        this.fboDatum = fboDatum;
    }

    @Override
    public boolean init(DrawContextIf context) {
        return true;
    }

    @Override
    public void beforeDraw(DrawContextIf context) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, context.retrieveDatum(fboDatum));
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    }

    @Override
    public void draw(DrawContextIf context) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, context.retrieveDatum(fboDatum));
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBlitFramebuffer(0,0,context.getScreenWidth(),context.getScreenHeight(),
                0,0,context.getScreenWidth(),context.getScreenHeight(),
                GL_COLOR_BUFFER_BIT,GL_LINEAR);
    }

    @Override
    public void afterDraw(DrawContextIf context) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    }
}
