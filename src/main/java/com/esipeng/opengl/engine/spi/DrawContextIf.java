package com.esipeng.opengl.engine.spi;

public interface DrawContextIf  extends ReleaseIf{
    void updateDatum(String key, int value);
    int retrieveDatum(String key);
    int getScreenWidth();
    int getScreenHeight();
    Iterable<DrawableObjectIf> getCurrentDrawableObject();
    //void updateModel(Matrix4f model);
}
