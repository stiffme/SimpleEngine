package com.esipeng.opengl.engine.base;

import java.nio.ByteBuffer;
import java.nio.file.Paths;

import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

public class TextureLoader {
    ByteBuffer data = null;
    int[] x, y, nrChannel;

    public ByteBuffer getData() {
        return data;
    }

    public int getX() {
        return x[0];
    }

    public int getY() {
        return y[0];
    }

    public int getNrChannel() {
        return nrChannel[0];
    }

    public TextureLoader() {
        x = new int[1];
        y = new int[1];
        nrChannel = new int[1];
    }

    public boolean loadFromResource(String resource)    {

        try {
            String resourcePath = Paths.get(getClass().getClassLoader().getResource(resource).toURI()).toAbsolutePath().toString();
            return loadFromFilePath(resourcePath);
        }catch ( Exception e)   {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadFromFilePath(String path)    {
        release();

        try {
            data = stbi_load(path, x, y, nrChannel, 0);
            if(data == null)
                return false;
        }catch ( Exception e)   {
            e.printStackTrace();
            return false;
        }
        return  true;
    }

    public void release()   {
        if(data != null)    {
            x[0] = y[0] = nrChannel[0] = 0;
            stbi_image_free(data);
        }
    }
}
