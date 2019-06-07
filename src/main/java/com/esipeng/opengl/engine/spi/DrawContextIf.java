package com.esipeng.opengl.engine.spi;

import org.joml.Matrix4f;

public interface DrawContextIf  extends ReleaseIf{
    void updateDatum(String key, int value);
    int retrieveDatum(String key);
    int getScreenWidth();
    int getScreenHeight();
    Iterable<DrawableObjectIf> getCurrentDrawableObject();
    void updateModel(Matrix4f model);
}
