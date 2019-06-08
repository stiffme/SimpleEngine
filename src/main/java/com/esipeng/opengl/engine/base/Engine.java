package com.esipeng.opengl.engine.base;

import com.esipeng.opengl.engine.shader.DebugTextRenderer;
import com.esipeng.opengl.engine.spi.DrawComponentIf;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBDebugOutput.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Engine
        extends ManagedObject
        implements DrawContextIf {
    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    private LinkedList<DrawComponentIf> components;
    private Map<String, Integer> datums;
    private int screenWidth, screenHeight;
    private DrawComponentIf currentComponent;
    private long window;
    private World world;
    private MVPManager mvpManager;
    private Camera camera;
    private float previousTime;
    private Matrix4f projection;
    private DebugTextRenderer debugTextRenderer;

    //for FPS
    private float timeElapsed = 0;
    private int frameCount = 0;

    public Engine(int width, int height)    {
        this.screenHeight = height;
        this.screenWidth = width;
        components = new LinkedList<>();
        mvpManager = new MVPManager();
        projection = new Matrix4f();
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
            assert mode != null;
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
        camera = new Camera(window);
        GL.createCapabilities();
        glClearColor(0f,0f,0f,1f);
        glViewport(0,0,screenWidth, screenHeight);
        if(debug)
            enableDebug();

    }

    private void enableDebug()  {
        GLCapabilities capabilities = GL.getCapabilities();
        if(capabilities.GL_ARB_debug_output)    {
            logger.debug("Debug supported!");
            glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);
            glDebugMessageCallbackARB(new GLDebugMessageARBCallback() {
                @Override
                public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
                    String strMsg = getMessage(length,message);
                    switch (severity)   {
                        case GL_DEBUG_SEVERITY_HIGH_ARB:
                            logger.error(strMsg);
                            break;
                        case GL_DEBUG_SEVERITY_MEDIUM_ARB:
                            logger.warn(strMsg);
                            break;
                        case GL_DEBUG_SEVERITY_LOW_ARB:
                            logger.info(strMsg);
                            break;
                    }
                }
            }, 0L);
        } else {
            logger.warn("Debug not supported!");
        }

        if(debugTextRenderer == null)   {
            debugTextRenderer = new DebugTextRenderer("debug","font/DejaVuSerif.ttf");
            if(!debugTextRenderer.init(this))   {
                logger.warn("DebugTextRenderer init failed!");
                debugTextRenderer = null;
            }
        }
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

        previousTime = (float)glfwGetTime();
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

    public void draw(World world) {
        //check if ESC is pressed
        if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);

        this.world = world;
        float currentT = (float)glfwGetTime();
        float elapsed = currentT - previousTime;
        previousTime = currentT;

        camera.processInput(elapsed);
        //set view
        mvpManager.updateView(camera.generateViewMat());
        //set projection
        projection.setPerspective(camera.getFovRadians(),(float)screenWidth/screenHeight,0.1f,100f);
        mvpManager.updateProjection(projection);

        datums.clear();
        for(DrawComponentIf drawComponentIf:components) {
            this.currentComponent = drawComponentIf;
            drawComponentIf.beforeDraw(this);
            drawComponentIf.draw(this);
            drawComponentIf.afterDraw(this);
        }

        //fps calculation
        if(debugTextRenderer != null)   {
            timeElapsed += elapsed;
            ++frameCount;

            if(timeElapsed > 1f) {
                float fps = frameCount / timeElapsed;
                String FPS = String.format("FPS: %.2f", fps);
                debugTextRenderer.setDebugContent(FPS);

                frameCount = 0;
                timeElapsed = 0f;
            }

            debugTextRenderer.beforeDraw(this);
            debugTextRenderer.draw(this);
            debugTextRenderer.afterDraw(this);
        }

        glfwSwapBuffers(window);
    }

    public boolean shouldCloseWindow()  {
        return glfwWindowShouldClose(window);
    }

    @Override
    public Iterable<DrawableObjectIf> getCurrentDrawableObject() {
        return this.world.getAllObjects();
    }

    @Override
    public void updateModel(Matrix4f model) {
        mvpManager.updateModel(model);
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
                //logger.debug("Updating datum {} to {}", key, value);
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
                //logger.debug("Retrieving datum {} from {}", key, value);
                return datums.get(key);
            }
        }
        return 0;
    }

    public void release()   {
        for(DrawComponentIf drawComponentIf : components)   {
            drawComponentIf.release();
        }
        super.release();
        glfwTerminate();
    }

    public void enableFpsView() {
        camera.enableMouseFpsView();
    }

    private boolean initUniformBlock()  {
        int dummyProgram;
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
}
