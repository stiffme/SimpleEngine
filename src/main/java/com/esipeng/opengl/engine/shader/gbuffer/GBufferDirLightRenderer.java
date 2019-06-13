package com.esipeng.opengl.engine.shader.gbuffer;

import com.esipeng.opengl.engine.base.DrawableObjectBase;
import com.esipeng.opengl.engine.base.Mesh;
import com.esipeng.opengl.engine.base.light.AbstractLight;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;

import java.util.ArrayList;
import java.util.List;

import static com.esipeng.opengl.engine.base.Constants.VERTEX_VEC2_TEXCOORD;
import static com.esipeng.opengl.engine.base.Constants.VERTEX_VEC3_POS;
import static org.lwjgl.opengl.GL33.*;

public class GBufferDirLightRenderer extends GBufferLightRendererBase {

    private class QuadObject extends DrawableObjectBase {
        private List<Mesh> meshes;

        public QuadObject() {
            super(1);
            this.meshes = new ArrayList<>();

            float[] quadVertices = { // vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates.
                    // positions   // texCoords
                    -1.0f,  1.0f,  0.0f, 1.0f,
                    -1.0f, -1.0f,  0.0f, 0.0f,
                    1.0f, -1.0f,  1.0f, 0.0f,

                    -1.0f,  1.0f,  0.0f, 1.0f,
                    1.0f, -1.0f,  1.0f, 0.0f,
                    1.0f,  1.0f,  1.0f, 1.0f
            };
            int vao = getManagedVAO();
            int vbo = getManagedVBO();
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER,quadVertices,GL_STATIC_DRAW);
            glVertexAttribPointer(VERTEX_VEC3_POS,2,GL_FLOAT,false,Float.BYTES * 4, 0);
            glVertexAttribPointer(VERTEX_VEC2_TEXCOORD,2,GL_FLOAT,false,Float.BYTES * 4, Float.BYTES * 2);
            glEnableVertexAttribArray(VERTEX_VEC3_POS);
            glEnableVertexAttribArray(VERTEX_VEC2_TEXCOORD);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
            Mesh mesh = new Mesh(vao,0,0,0,0,6);
            meshes.add(mesh);
        }

        @Override
        public Iterable<Mesh> getMeshes() {
            return meshes;
        }
    }

    public GBufferDirLightRenderer(String name) {
        super(name);
    }

    @Override
    protected int compileProgram(DrawContextIf context) {
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

    @Override
    protected void handleOneLight(AbstractLight light, DrawableObjectIf drawableObject) {
        //do nothing
    }

    protected DrawableObjectIf buildDrawableObject()    {
        return new QuadObject();


    }
}
