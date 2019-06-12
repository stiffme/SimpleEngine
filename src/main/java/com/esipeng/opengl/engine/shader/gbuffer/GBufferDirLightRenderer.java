package com.esipeng.opengl.engine.shader.gbuffer;

import com.esipeng.opengl.engine.base.light.AbstractLight;
import com.esipeng.opengl.engine.spi.DrawContextIf;

public class GBufferDirLightRenderer extends GBufferLightRendererBase {
    public GBufferDirLightRenderer(String name) {
        super(name);
    }

    @Override
    protected int compileProgram() {
        try {
            int p = compileAndLinkProgram(
                    "shader/GBufferLightRenderer/DirLight/vertex.glsl",
                    "shader/GBufferLightRenderer/DirLight/fragment.glsl"
            );
            return p;
        } catch (Exception e)   {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    protected Iterable<AbstractLight> getLights(DrawContextIf context) {
        return context.getWorld().getDirLights();
    }
}
