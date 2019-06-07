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
import static com.esipeng.opengl.engine.base.Constants.*;
import static org.lwjgl.opengl.GL33.*;

import java.nio.file.Files;
import java.nio.file.Paths;
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
    private DrawableObjectIf currentDrawableObject;
    private int dummyProgram;
    private MVPManager mvpManager;


    public Engine(int width, int height)    {
        this.screenHeight = height;
        this.screenWidth = width;
        components = new LinkedList<>();
        mvpManager = new MVPManager();
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
        if(!initUniformBlock())
            return false;

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
        currentDrawableObject = drawableObjectIf;
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
    public DrawableObjectIf getCurrentDrawableObject() {
        return currentDrawableObject;
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

        glDeleteProgram(dummyProgram);
        dummyProgram = 0;

        glfwTerminate();
    }

    private boolean initUniformBlock()  {
        try {
            dummyProgram = compileAndLinkProgram(
                    "shader/DummyUniformBlock/vertex.glsl",
                    "shader/DummyUniformBlock/fragment.glsl"
            );
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        return mvpManager.bindProgram(dummyProgram);
    }

    private int linkProgram(int vShader, int fShader)    {
        int program = glCreateProgram();
        if(program == 0)
            return 0;

        glAttachShader(program, vShader);
        glAttachShader(program, fShader);
        glLinkProgram(program);

        int linkStatus = glGetProgrami(program, GL_LINK_STATUS);
        if(linkStatus != GL_TRUE)   {
            logger.error("Link failed {}" ,
                    glGetProgramInfoLog(program));
            return 0;
        }
        return program;
    }

    private int compileAndLinkProgram(String vShaderPath, String fShaderPath) throws Exception    {
        String vShaderSrc = loadFileFromResource(vShaderPath);
        String fShaderSrc = loadFileFromResource(fShaderPath);
        int vShader = loadShader(GL_VERTEX_SHADER, vShaderSrc);
        int fShader = loadShader(GL_FRAGMENT_SHADER, fShaderSrc);
        int program = linkProgram(vShader, fShader);
        return program;
    }

    private String loadFileFromResource(String resource ) throws Exception {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resource).toURI())));
    }

    private int loadShader(int type, String shaderSrc) throws Exception    {
        int shader = glCreateShader(type);
        glShaderSource(shader,shaderSrc);
        glCompileShader(shader);

        int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
        if(compiled != GL_TRUE) {
            logger.error("Failed to compile shader {}! ", shaderSrc );
            logger.error(glGetShaderInfoLog(shader));
            throw new Exception("Failed to compile shader");
        }
        return shader;

    }
}
