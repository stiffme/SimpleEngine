package com.esipeng.opengl.engine.demo;

import com.esipeng.opengl.engine.base.Engine;
import com.esipeng.opengl.engine.base.World;
import com.esipeng.opengl.engine.base.light.DirectionalLight;
import com.esipeng.opengl.engine.importer.ModelImporter;
import com.esipeng.opengl.engine.importer.NormalBrick;
import com.esipeng.opengl.engine.shader.DebugWindowsRenderer;
import com.esipeng.opengl.engine.shader.OutputScreen;
import com.esipeng.opengl.engine.shader.gbuffer.GBufferCompositor;
import com.esipeng.opengl.engine.shader.gbuffer.GBufferDirLightRenderer;
import com.esipeng.opengl.engine.shader.gbuffer.GBufferInputRenderer;
import com.esipeng.opengl.engine.spi.DrawComponentIf;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;
import org.joml.Vector3f;

import java.util.LinkedList;
import java.util.List;

import static com.esipeng.opengl.engine.base.Constants.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;

public class NanosuitDemoDeferred {
    private static final int NUM_INSTANCE = 25;

    public static void main(String[] args)  {
        Engine engine = new Engine(1024,768);
        engine.createGLContext(false, true);

        DrawComponentIf gBufferRenderer = new GBufferInputRenderer(
                "GBuffer"
        );

        DrawComponentIf gBufferCompositor = new GBufferCompositor("compositor");

        DrawComponentIf gbufferDirLightRenderer = new GBufferDirLightRenderer("Direction");

        DrawComponentIf outputScreen = new OutputScreen("screen", GBUFFER_COMPOSITOR_TEXTURE);

        List<String> debugDatums = new LinkedList<>();
        debugDatums.add(GBUFFER_POSITION);
        debugDatums.add(GBUFFER_NORMAL);
        debugDatums.add(GBUFFER_AMBIENT);
        debugDatums.add(GBUFFER_ALBEDOSPEC);
        DebugWindowsRenderer debugWindowsRenderer = new DebugWindowsRenderer("debugWindow", debugDatums);

        //engine.addDrawComponent(plainInputRenderer).addDrawComponent(outputScreen);
        engine.addDrawComponent(gBufferRenderer)
                .addDrawComponent(gBufferCompositor)
                .addDrawComponent(gbufferDirLightRenderer)
                .addDrawComponent(debugWindowsRenderer)
                .addDrawComponent(outputScreen);

        if(!engine.initAllComponents())
            return ;
        debugWindowsRenderer.setAlpha(0.5f);

        World world = new World();
        ModelImporter nanoSuit = new ModelImporter();
        if(!nanoSuit.loadFromLocation("/home/stiffme/models/nanosuitRef/nanosuit.obj"))
            return ;

        nanoSuit.setInstances(NUM_INSTANCE);

        int lenth = (int)Math.sqrt(NUM_INSTANCE);
        float gap = 10.f / lenth;
        for(int i = 0; i < NUM_INSTANCE; ++i)   {
            int line = i % lenth;
            int col = i / lenth;
            nanoSuit.setScale(i, 0.1f);
            nanoSuit.setPosition(i, -5.f + gap * line, -5f + gap * col, 0.0f);
            nanoSuit.setRotate(i, (float)Math.toRadians(i),1.0f,1.0f,1.0f);
        }

        world.addObject(nanoSuit);
        DrawableObjectIf wall1 = new NormalBrick(true);
        DrawableObjectIf wall2 = new NormalBrick(false);
        wall1.setPosition(-1,0,-1);
        wall2.setPosition(1,0,-1);
        world.addObject(wall1);
        world.addObject(wall2);
        DirectionalLight dirLight = new DirectionalLight(new Vector3f(0.0f),
                new Vector3f(0.5f,0,0),
                new Vector3f(0.2f),
                new Vector3f(1,0,-1));

        DirectionalLight dirLight2 = new DirectionalLight(new Vector3f(0.0f),
                new Vector3f(0,0.5f,0),
                new Vector3f(0.2f),
                new Vector3f(-1,0,-1));
        world.addDirLight(dirLight);
        world.addDirLight(dirLight2);


        while(!engine.shouldCloseWindow())   {
            glfwPollEvents();


            for(int i = 0; i < NUM_INSTANCE; ++i)   {
                nanoSuit.setRotate(i, (float)Math.toRadians(i + glfwGetTime() * 25),0.0f,1.0f,0.0f);
            }
            wall1.setRotate( (float)Math.toRadians( glfwGetTime() * 25),0.0f,1.0f,0.0f);
            wall2.setRotate( (float)Math.toRadians( glfwGetTime() * 25),0.0f,1.0f,0.0f);
            engine.draw(world);
        }

        engine.release();
        world.release();
    }
}
