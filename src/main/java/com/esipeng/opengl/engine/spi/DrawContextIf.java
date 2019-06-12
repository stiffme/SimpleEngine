package com.esipeng.opengl.engine.spi;

import com.esipeng.opengl.engine.base.World;

public interface DrawContextIf  extends ReleaseIf{
    void updateDatum(String key, int value);
    int retrieveDatum(String key);
    int getScreenWidth();
    int getScreenHeight();
    //Iterable<DrawableObjectIf> getCurrentDrawableObject();
    World getWorld();
    //void updateModel(Matrix4f model);
}
