package com.esipeng.opengl.engine.spi;

import java.util.Set;

public interface DrawComponentIf {

    /**
     * Initialize the DrawComponent
     * @return true when successful
     */
    boolean init(DrawContextIf context);

    /**
     * bind needed data before draw calls
     * @param context the drawing context
     */
    void beforeDraw(DrawContextIf context);

    /**
     * draw the data
     * @param context the drawing context
     */
    void draw(DrawContextIf context);

    /**
     * unbind needed data after draw calls
     * @param context the drawing context
     */
    void afterDraw(DrawContextIf context);

    /**
     * release all resources
     * @param context drawing context
     */
    void release(DrawContextIf context);

    /**
     * get all input datum
     * @return the set of input datum
     */
    Set<String> getInputDatum();

    /**
     * get all output datum
     * @return the set of output datum
     */
    Set<String> getOutputDatum();

    /**
     * get name of the draw component
     * @return the name of the component
     */
    String getName();
}
