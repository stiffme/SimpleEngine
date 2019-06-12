package com.esipeng.opengl.engine.importer;

import com.esipeng.opengl.engine.base.DrawableObjectBase;
import com.esipeng.opengl.engine.base.Mesh;
import com.esipeng.opengl.engine.base.TextureLoader;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.LinkedList;
import java.util.List;

import static com.esipeng.opengl.engine.base.Constants.*;
import static org.lwjgl.opengl.GL30.*;

public class NormalBrick extends DrawableObjectBase {

    List<Mesh> meshes = new LinkedList<>();


    public NormalBrick(boolean useNormal)    {
        super(1);
        init(useNormal);
    }

    private void init(boolean useNormal) {
        //positions
        Vector3f pos1 = new Vector3f(-1.0f,1.0f,0.0f);
        Vector3f pos2 = new Vector3f(-1.0f,-1.0f,0.0f);
        Vector3f pos3 = new Vector3f(1.0f,-1.0f,0.0f);
        Vector3f pos4 = new Vector3f(1.0f,1.0f,0.0f);

        Vector2f uv1 = new Vector2f(0.0f, 1.0f);
        Vector2f uv2 = new Vector2f(0.0f, 0.0f);
        Vector2f uv3 = new Vector2f(1.0f, 0.0f);
        Vector2f uv4 = new Vector2f(1.0f, 1.0f);

        Vector3f nm = new Vector3f(0.0f,0.0f,1.0f);

        Vector3f tangent1 = new Vector3f(), tangent2 = new Vector3f(),
                bitangent1 = new Vector3f(), bitangent2 = new Vector3f();

        Vector3f edge1 = new Vector3f(), edge2 = new Vector3f();
        Vector2f deltaUV1 = new Vector2f(), deltaUV2 = new Vector2f();

        //triangle 1
        pos2.sub(pos1, edge1);
        pos3.sub(pos1, edge2);

        uv2.sub(uv1, deltaUV1);
        uv3.sub(uv1, deltaUV2);

        float f = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y);
        tangent1.x = f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x);
        tangent1.y = f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y);
        tangent1.z = f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z);
        tangent1.normalize();

        bitangent1.x = f * (-deltaUV2.x * edge1.x + deltaUV1.x * edge2.x);
        bitangent1.y = f * (-deltaUV2.x * edge1.y + deltaUV1.x * edge2.y);
        bitangent1.z = f * (-deltaUV2.x * edge1.z + deltaUV1.x * edge2.z);
        bitangent1.normalize();

        //triangle 2
        pos3.sub(pos1, edge1);
        pos4.sub(pos1, edge2);

        uv3.sub(uv1, deltaUV1);
        uv4.sub(uv1, deltaUV2);

        f = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y);
        tangent2.x = f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x);
        tangent2.y = f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y);
        tangent2.z = f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z);
        tangent2.normalize();

        bitangent2.x = f * (-deltaUV2.x * edge1.x + deltaUV1.x * edge2.x);
        bitangent2.y = f * (-deltaUV2.x * edge1.y + deltaUV1.x * edge2.y);
        bitangent2.z = f * (-deltaUV2.x * edge1.z + deltaUV1.x * edge2.z);
        bitangent2.normalize();

        float[] quadVertices = {
                // Positions            // normal         // TexCoords  // Tangent                          // Bitangent
                pos1.x, pos1.y, pos1.z, nm.x, nm.y, nm.z, uv1.x, uv1.y, tangent1.x, tangent1.y, tangent1.z, bitangent1.x, bitangent1.y, bitangent1.z,
                pos2.x, pos2.y, pos2.z, nm.x, nm.y, nm.z, uv2.x, uv2.y, tangent1.x, tangent1.y, tangent1.z, bitangent1.x, bitangent1.y, bitangent1.z,
                pos3.x, pos3.y, pos3.z, nm.x, nm.y, nm.z, uv3.x, uv3.y, tangent1.x, tangent1.y, tangent1.z, bitangent1.x, bitangent1.y, bitangent1.z,

                pos1.x, pos1.y, pos1.z, nm.x, nm.y, nm.z, uv1.x, uv1.y, tangent2.x, tangent2.y, tangent2.z, bitangent2.x, bitangent2.y, bitangent2.z,
                pos3.x, pos3.y, pos3.z, nm.x, nm.y, nm.z, uv3.x, uv3.y, tangent2.x, tangent2.y, tangent2.z, bitangent2.x, bitangent2.y, bitangent2.z,
                pos4.x, pos4.y, pos4.z, nm.x, nm.y, nm.z, uv4.x, uv4.y, tangent2.x, tangent2.y, tangent2.z, bitangent2.x, bitangent2.y, bitangent2.z
        };

//        public static final int VERTEX_MAT_MODEL = 0;
//        public static final int VERTEX_VEC3_POS = 4;
//        public static final int VERTEX_VEC2_TEXCOORD = 5;
//        public static final int VERTEX_VEC3_NORMAL = 6;
//        public static final int VERTEX_VEC3_TANGENT = 7;
//        public static final int VERTEX_VEC3_BITANGENT = 8;

        int mQuadVAO = getManagedVAO();
        int vbo = getManagedVBO();
        glBindVertexArray(mQuadVAO);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
        glVertexAttribPointer(VERTEX_VEC3_POS,3,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 0);
        glVertexAttribPointer(VERTEX_VEC3_NORMAL,3,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 3);
        glVertexAttribPointer(VERTEX_VEC2_TEXCOORD,2,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 6);
        glVertexAttribPointer(VERTEX_VEC3_TANGENT,3,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 8);
        glVertexAttribPointer(VERTEX_VEC3_BITANGENT,3,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 11);
        glEnableVertexAttribArray(VERTEX_VEC3_POS);
        glEnableVertexAttribArray(VERTEX_VEC3_NORMAL);
        glEnableVertexAttribArray(VERTEX_VEC2_TEXCOORD);
        glEnableVertexAttribArray(VERTEX_VEC3_TANGENT);
        glEnableVertexAttribArray(VERTEX_VEC3_BITANGENT);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        int mDiffuseMap = loadTextureFromResource("textures/brickwall.jpg");
        int mNormalMap = 0;
        if(useNormal)
            mNormalMap = loadTextureFromResource("textures/brickwall_normal.jpg");
        int mSpecular = loadTextureFromResource("textures/brickwall_specular.jpg");

        Mesh mesh = new Mesh(mQuadVAO,0,mDiffuseMap, mSpecular,mNormalMap, 6,96*4,false);
        meshes.add(mesh);
    }

    protected int loadTextureFromResource(String resoure) {
        TextureLoader loader = new TextureLoader();
        try {
            if(!loader.loadFromResource(resoure))
                return -1;

            int tex = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, tex);
            glTexImage2D(GL_TEXTURE_2D,0,GL_RGB,loader.getX(),loader.getY(),0,GL_RGB,
                    GL_UNSIGNED_BYTE,loader.getData());
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glBindTexture(GL_TEXTURE_2D, 0);
            return tex;
        } catch (Exception e)   {
            e.printStackTrace();
            return -1;
        } finally {
            loader.release();
        }
    }


    @Override
    public Iterable<Mesh> getMeshes() {
        return meshes;
    }
}
