package com.esipeng.opengl.engine.base;

import com.esipeng.opengl.engine.shader.DebugTextRenderer;
import com.esipeng.opengl.engine.spi.DrawComponentIf;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.remotery.Remotery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBDebugOutput.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.remotery.RemoteryGL.*;

public class Engine
        extends ManagedObject
        implements DrawContextIf {
    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    private LinkedList<DrawComponentIf> components;
    private Map<String, Integer> datums;
    private int screenWidth, screenHeight, windowedWidth,windowedHeight, windowedRefreshRate, windowedPosX, windowedPosY;
    private DrawComponentIf currentComponent;
    private long window;
    private World world;
    private MVPManager mvpManager;
    private Camera camera;
    private float previousTime;
    private Matrix4f projection;
    private DebugTextRenderer debugTextRenderer;
    private PointerBuffer remotery = null;
    private IntBuffer remoteryHashCode = null;
    private boolean f12Pressed = false;

    //for FPS
    private float timeElapsed = 0;
    private int frameCount = 0;

    public Engine(int width, int height)    {
        this.screenHeight =  this.windowedHeight = height;
        this.screenWidth = this.windowedWidth = width;
        components = new LinkedList<>();
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


        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);
        assert mode != null;
        this.windowedRefreshRate = mode.refreshRate();

        if(!fullScreen) {
            window = glfwCreateWindow(screenWidth, screenHeight,"Simple Engine", NULL, NULL);
        } else  {
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

    public void enableProfiling()   {
        if(remotery == null)    {
            remotery = PointerBuffer.allocateDirect(1);
            Remotery.rmt_CreateGlobalInstance(remotery);
            glfwSwapInterval(0);
            rmt_BindOpenGL();
            remoteryHashCode = IntBuffer.allocate(1);

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

        if(!initializeComponents())
            return false;
        //add resize handler
        glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                Engine.this.screenHeight = height;
                Engine.this.screenWidth = width;
                initializeComponents();
                glViewport(0,0,width,height);
            }
        });
        //validate
        return validateChain();
    }

    private boolean initializeComponents()  {
        //first initialize all the components
        for(DrawComponentIf drawComponentIf : components)   {
            drawComponentIf.release();
            if(!drawComponentIf.init(this))
                return false;
        }

        previousTime = (float)glfwGetTime();
        return true;
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

    private void toggleFullscreen() {
        if(glfwGetWindowMonitor(window) != NULL)    {
            //to windowed mode
            glfwSetWindowMonitor(window, NULL, windowedPosX, windowedPosY, windowedWidth, windowedHeight, windowedRefreshRate);
            camera.disableMouseFpsView();
        } else  {
            //to full screen rate
            try(MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer i1 = stack.callocInt(1);
                IntBuffer i2 = stack.callocInt(2);
                glfwGetWindowPos(window,i1, i2);
                windowedPosX = i1.get(0);
                windowedPosY = i2.get(0);
                glfwGetWindowSize(window,i1, i2);
                windowedWidth = i1.get(0);
                windowedHeight = i2.get(0);
            }

            long monitor = glfwGetPrimaryMonitor();
            if(monitor != NULL) {
                GLFWVidMode mode = glfwGetVideoMode(monitor);
                glfwSetWindowMonitor(window, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
                camera.enableMouseFpsView();
            }
        }
    }

    public void draw(World world) {
        //check if ESC is pressed
        if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
        //check if F12 is pressed
        if(glfwGetKey(window, GLFW_KEY_F12) == GLFW_PRESS)
            this.f12Pressed = true;
        if(glfwGetKey(window, GLFW_KEY_F12) == GLFW_RELEASE) {
            if(f12Pressed)  {
                toggleFullscreen();
                f12Pressed = false;
            }

        }

        this.world = world;
        float currentT = (float)glfwGetTime();
        float elapsed = currentT - previousTime;
        previousTime = currentT;

        //Profiling
        if(remotery != null)    {
            rmt_BeginOpenGLSample("Simple Engine",remoteryHashCode);
        }

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
        rmt_EndOpenGLSample();
    }

    public boolean shouldCloseWindow()  {
        return glfwWindowShouldClose(window);
    }

    @Override
    public Iterable<DrawableObjectIf> getCurrentDrawableObject() {
        return this.world.getAllObjects();
    }

//    @Override
//    public void updateModel(Matrix4f model) {
//        mvpManager.updateModel(model);
//    }

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
        if(remotery != null)
            rmt_UnbindOpenGL();

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
        mvpManager = new MVPManager();
        return mvpManager.bindProgram(dummyProgram);
    }
}
