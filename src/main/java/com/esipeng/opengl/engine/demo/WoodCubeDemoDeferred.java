package com.esipeng.opengl.engine.demo;

import com.esipeng.opengl.engine.base.Engine;
import com.esipeng.opengl.engine.base.World;
import com.esipeng.opengl.engine.importer.WoodCube;
import com.esipeng.opengl.engine.shader.DebugWindowsRenderer;
import com.esipeng.opengl.engine.shader.GBufferInputRenderer;
import com.esipeng.opengl.engine.shader.OutputScreen;
import com.esipeng.opengl.engine.spi.DrawComponentIf;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;

import java.util.LinkedList;
import java.util.List;

import static com.esipeng.opengl.engine.base.Constants.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;

public class WoodCubeDemoDeferred {
    private static final int NUM_INSTANCE = 100;

    public static void main(String[] args)  {
        Engine engine = new Engine(1024,768);
        engine.createGLContext(false, true);

        //1, plain input renderer
        DrawComponentIf gBufferRenderer = new GBufferInputRenderer(
                "GBuffer"
        );

        //2, output to screen
        DrawComponentIf outputScreen = new OutputScreen(
                "screen",GBUFFER_AMBIENT
        );

        List<String> debugDatums = new LinkedList<>();
        debugDatums.add(GBUFFER_POSITION);
        debugDatums.add(GBUFFER_NORMAL);
        debugDatums.add(GBUFFER_AMBIENT);
        debugDatums.add(GBUFFER_ALBEDOSPEC);
        DebugWindowsRenderer debugWindowsRenderer = new DebugWindowsRenderer("debugWindow", debugDatums);


        engine.addDrawComponent(gBufferRenderer)
                .addDrawComponent(outputScreen)
                .addDrawComponent(debugWindowsRenderer);

        if(!engine.initAllComponents())
            return ;
        //engine.enableProfiling();
        debugWindowsRenderer.setAlpha(0.5f);

        World world = new World();
        DrawableObjectIf woodCube = new WoodCube();
        woodCube.setInstances(NUM_INSTANCE);

        int lenth = (int)Math.sqrt(NUM_INSTANCE);
        float gap = 10.f / lenth;
        for(int i = 0; i < NUM_INSTANCE; ++i)   {
            int line = i % lenth;
            int col = i / lenth;
            woodCube.setScale(i, 0.4f);
            woodCube.setPosition(i, -5.f + gap * line, -5f + gap * col, 0.0f);
            woodCube.setRotate(i, (float)Math.toRadians(i),1.0f,1.0f,1.0f);
        }

        world.addObject(woodCube);
        //engine.enableFpsView();

        while(!engine.shouldCloseWindow())   {
            glfwPollEvents();


            for(int i = 0; i < NUM_INSTANCE; ++i)   {
                woodCube.setRotate(i, (float)Math.toRadians(i + glfwGetTime() * 200),1.0f,1.0f,1.0f);
            }
            engine.draw(world);
        }

        engine.release();
        world.release();
    }
}
