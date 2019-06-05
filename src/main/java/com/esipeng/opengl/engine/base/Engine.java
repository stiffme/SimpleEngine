package com.esipeng.opengl.engine.base;

import com.esipeng.opengl.engine.spi.DrawComponentIf;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Engine implements DrawContextIf {
    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    private LinkedList<DrawComponentIf> components;
    private Map<String, Integer> datums;
    private int screenWidth, screenHeight;
    private DrawComponentIf currentComponent;
    private long window;

    public Engine(int width, int height)    {
        this.screenHeight = height;
        this.screenWidth = width;
        components = new LinkedList<>();

    }

    public void createGLContext(boolean fullScreen, boolean debug)   {
        if(!glfwInit())
            throw new RuntimeException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        if(debug)
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, 1);

        if(!fullScreen) {
            window = glfwCreateWindow(screenWidth, screenHeight,"Simple Engine", NULL, NULL);
        } else  {
            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode mode = glfwGetVideoMode(monitor);
            glfwWindowHint(GLFW_RED_BITS, mode.redBits());
            glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
            glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
            glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
            screenWidth = mode.width();
            screenHeight = mode.height();
            window = glfwCreateWindow(screenWidth, screenHeight,"Simple Engine", monitor, NULL);
        }
        if(window == NULL)
            throw new RuntimeException("Window is not created!");

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public Engine addDrawComponent(DrawComponentIf drawComponentIf)  {
        components.add(drawComponentIf);
        return this;
    }

    public Engine addDrawComponent(int index, DrawComponentIf drawComponentIf)    {
        components.add(index, drawComponentIf);
        return this;
    }

    public boolean initAllComponents()  {
        datums = new HashMap<>();

        //first initialize all the components
        for(DrawComponentIf drawComponentIf : components)   {
            if(!drawComponentIf.init(this))
                return false;
        }
        //validate
        return validateChain();
    }

    /**
     * This function validates all the draw chain
     * @return true when successful
     */
    private boolean validateChain()  {
        Map<String, Integer> tempDatums = new HashMap<>();
        for(DrawComponentIf drawComponentIf : components)   {
            //validates input datums
            for(String inputDatum : drawComponentIf.getInputDatum())    {
                if(!tempDatums.containsKey(inputDatum)) {
                    logger.error("{} needs input datum {}, not found", drawComponentIf.getName(), inputDatum);
                    return false;
                }
            }

            //output datum
            for(String outputDatum: drawComponentIf.getOutputDatum())   {
                if(tempDatums.containsKey(outputDatum)) {
                    logger.warn("{} overwrites datum {}", drawComponentIf.getName(), outputDatum);
                }
                tempDatums.put(outputDatum, 0);
            }
        }

        return true;
    }

    public void draw(DrawableObjectIf drawableObjectIf) {
        datums.clear();
        for(DrawComponentIf drawComponentIf:components) {
            this.currentComponent = drawComponentIf;
            drawComponentIf.beforeDraw(this);
            drawComponentIf.draw(this);
            drawComponentIf.afterDraw(this);
        }
        glfwSwapBuffers(window);
    }

    @Override
    public void updateDatum(String key, int value) {
        if(currentComponent == null)    {
            logger.warn("Current Component is null, skipping datum.");
        } else  {
            if(!currentComponent.getOutputDatum().contains(key))    {
                logger.error("Component {} writes to datum {} illegally",
                        currentComponent.getName(),
                        key);
            } else  {
                //updating datum
                logger.debug("Updating datum {} to {}", key, value);
                datums.put(key, value);
            }
        }
    }

    @Override
    public int retrieveDatum(String key) {
        if(currentComponent == null)    {
            logger.warn("Current Component is null, skipping datum.");
        } else  {
            if(!currentComponent.getInputDatum().contains(key))    {
                logger.error("Component {} reads from datum {} illegally",
                        currentComponent.getName(),
                        key);
            } else  {
                //retrieving datum
                int value = datums.get(key);
                logger.debug("Retrieving datum {} from {}", key, value);
                return value;
            }
        }
        return 0;
    }

    public void release()   {
        for(DrawComponentIf drawComponentIf : components)   {
            drawComponentIf.release(this);
        }

        glfwTerminate();
    }
}
