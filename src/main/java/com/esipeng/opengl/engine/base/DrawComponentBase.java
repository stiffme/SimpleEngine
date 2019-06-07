package com.esipeng.opengl.engine.base;

import com.esipeng.opengl.engine.spi.DrawComponentIf;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class DrawComponentBase
        extends ManagedObject
        implements DrawComponentIf {
    private static final Logger logger = LoggerFactory.getLogger(DrawComponentBase.class);
    private Set<String> inputDatum, outputDatum;
    private String name;

    protected DrawComponentBase(
            String name,
            Set<String> inputDatum,
            Set<String> outputDatum
    )   {
        this.inputDatum = inputDatum;
        this.outputDatum = outputDatum;
        this.name = name;
    }

    public boolean init(DrawContextIf context) {
        return false;
    }

    public void beforeDraw(DrawContextIf context) {

    }

    public void draw(DrawContextIf context) {

    }

    public void afterDraw(DrawContextIf context) {

    }

    public Set<String> getInputDatum() {
        return inputDatum;
    }

    public Set<String> getOutputDatum() {
        return outputDatum;
    }

    public String getName() {
        return name;
    }



}
