package com.esipeng.opengl.engine.base;

import com.esipeng.opengl.engine.spi.DrawableObjectIf;

import java.util.HashSet;
import java.util.Set;

public class World {
    private Set<DrawableObjectIf> objects = new HashSet<>();

    public void addObject(DrawableObjectIf object)  {
        objects.add(object);
    }

    public Iterable<DrawableObjectIf> getAllObjects()   {
        return objects;
    }

    public void release()   {
        for(DrawableObjectIf object : objects)
            object.release();
    }
}
