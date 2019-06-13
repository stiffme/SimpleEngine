package com.esipeng.opengl.engine.base;

import com.esipeng.opengl.engine.base.light.AbstractLight;
import com.esipeng.opengl.engine.base.light.DirectionalLight;
import com.esipeng.opengl.engine.base.light.PointLight;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;

import java.util.LinkedList;
import java.util.List;

public class World {
    private List<DrawableObjectIf> objects = new LinkedList<>();
    private List<AbstractLight> dirLights = new LinkedList<>();
    private List<AbstractLight> pointLights = new LinkedList<>();

    public void addObject(DrawableObjectIf object)  {
        objects.add(object);
    }
    public void addDirLight(DirectionalLight light) {
        dirLights.add(light);
    }

    public void addPointLight(PointLight light) {
        pointLights.add(light);
    }

    public Iterable<DrawableObjectIf> getAllObjects()   {
        return objects;
    }
    public Iterable<AbstractLight> getDirLights()   {
        return dirLights;
    }
    public Iterable<AbstractLight> getPointLights()    {
        return pointLights;
    }

    public void release()   {
        for(DrawableObjectIf object : objects)
            object.release();

        for(AbstractLight light : dirLights)
            light.release();

    }
}
