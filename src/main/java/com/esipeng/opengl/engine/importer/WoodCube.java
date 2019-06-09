package com.esipeng.opengl.engine.importer;

import com.esipeng.opengl.engine.base.DrawableObjectBase;
import com.esipeng.opengl.engine.base.Mesh;
import com.esipeng.opengl.engine.base.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;

import static com.esipeng.opengl.engine.base.Constants.*;
import static org.lwjgl.opengl.GL33.*;


public class WoodCube extends DrawableObjectBase {
    public static WoodCube woodCube = null;
    private static Logger logger = LoggerFactory.getLogger(WoodCube.class);

    private List<Mesh> meshes;
    public WoodCube()   {
        this(1);
    }

    public WoodCube(int instaceNum)    {
        super(instaceNum);
        float[] cubeVertices = {
                // Back face
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, // Bottom-left
                0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f, // top-right
                0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, // bottom-right
                0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,  // top-right
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,  // bottom-left
                -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f,// top-left
                // Front face
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, // bottom-left
                0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,  // bottom-right
                0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,  // top-right
                0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // top-right
                -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,  // top-left
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,  // bottom-left
                // Left face
                -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // top-right
                -0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // top-left
                -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,  // bottom-left
                -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, // bottom-left
                -0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f,  // bottom-right
                -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // top-right
                // Right face
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // top-left
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, // bottom-right
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // top-right
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,  // bottom-right
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,  // top-left
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, // bottom-left
                // Bottom face
                -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, // top-right
                0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 1.0f, // top-left
                0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,// bottom-left
                0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, // bottom-left
                -0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, // bottom-right
                -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, // top-right
                // Top face
                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,// top-left
                0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // bottom-right
                0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, // top-right
                0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // bottom-right
                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,// top-left
                -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f // bottom-left
        };
        int mCubeVAO = getManagedVAO();
        int vbo = getManagedVBO();
        glBindVertexArray(mCubeVAO);
        glBindBuffer(GL_ARRAY_BUFFER,vbo);
        glBufferData(GL_ARRAY_BUFFER,cubeVertices,GL_STATIC_DRAW);
        glVertexAttribPointer(VERTEX_VEC3_POS,3,GL_FLOAT,false,Float.BYTES*8, 0L);
        glVertexAttribPointer(VERTEX_VEC3_NORMAL,3,GL_FLOAT,false, Float.BYTES*8, Float.BYTES * 3);
        glVertexAttribPointer(VERTEX_VEC2_TEXCOORD,2,GL_FLOAT,false,Float.BYTES*8,Float.BYTES * 6);
        glEnableVertexAttribArray(VERTEX_VEC3_POS);
        glEnableVertexAttribArray(VERTEX_VEC3_NORMAL);
        glEnableVertexAttribArray(VERTEX_VEC2_TEXCOORD);
        glBindBuffer(GL_ARRAY_BUFFER,0);
        glBindVertexArray(0);

        //load texture
        try {
            TextureLoader textureLoader = new TextureLoader();
            if(!textureLoader.loadFromResource("textures/container.jpg"))
                logger.error("Loading container.jpg failed!");

            int woodTexture = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, woodTexture);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB,
                    textureLoader.getX(), textureLoader.getY(), 0,
                    GL_RGB, GL_UNSIGNED_BYTE, textureLoader.getData());
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            glBindTexture(GL_TEXTURE_2D, 0);
            Mesh mesh = new Mesh(mCubeVAO, woodTexture,
                    woodTexture,woodTexture,0,36);
            this.meshes  = Arrays.asList(mesh);
        } catch (Exception e)   {
            logger.error(e.getMessage(), e);
        }


    }

    @Override
    public Iterable<Mesh> getMeshes() {
        return meshes;
    }

    public static WoodCube getInstance()    {
        if(woodCube == null)
            woodCube = new WoodCube();

        return woodCube;
    }
}
