package com.esipeng.opengl.engine.spi;

public interface DrawContextIf {
    void updateDatum(String key, int value);
    int retrieveDatum(String key);
    int getScreenWidth();
    int getScreenHeight();
}
