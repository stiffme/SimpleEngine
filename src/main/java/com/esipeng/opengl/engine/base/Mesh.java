package com.esipeng.opengl.engine.base;
import static org.lwjgl.opengl.GL33.*;

public class Mesh {
    private int vao;
    private int ambient;
    private int diffuse;
    private int specular;
    private int normal;
    private int verticesNumber;

    public int getVerticesNumber() {
        return verticesNumber;
    }

    public int getVao() {
        return vao;
    }

    public int getAmbient() {
        return ambient;
    }

    public int getDiffuse() {
        return diffuse;
    }

    public int getSpecular() {
        return specular;
    }

    public int getNormal() {
        return normal;
    }

    void release()   {
        if(vao != 0)
            glDeleteVertexArrays(vao);

        if(ambient != 0)
            glDeleteTextures(ambient);

        if(diffuse != 0)
            glDeleteTextures(diffuse);

        if(specular != 0)
            glDeleteTextures(specular);

        if(normal != 0)
            glDeleteTextures(normal);

        vao = ambient = diffuse = specular = normal = 0;
        verticesNumber = 0;
    }

    public Mesh(int vao, int ambient, int diffuse, int specular, int normal, int verticesNumber) {
        this.vao = vao;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.normal = normal;
        this.verticesNumber = verticesNumber;
    }
}
