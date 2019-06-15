package com.esipeng.opengl.engine.demo;

import com.esipeng.opengl.engine.base.Engine;
import com.esipeng.opengl.engine.base.World;
import com.esipeng.opengl.engine.base.light.PointLight;
import com.esipeng.opengl.engine.importer.ModelImporter;
import com.esipeng.opengl.engine.shader.DebugWindowsRenderer;
import com.esipeng.opengl.engine.shader.OutputScreenFBO;
import com.esipeng.opengl.engine.shader.gbuffer.GBufferDirLightRenderer;
import com.esipeng.opengl.engine.shader.gbuffer.GBufferInputRenderer;
import com.esipeng.opengl.engine.shader.gbuffer.GBufferPointLightRenderer;
import com.esipeng.opengl.engine.spi.DrawComponentIf;
import org.joml.Vector3f;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.esipeng.opengl.engine.base.Constants.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;

public class NanosuitDemoDeferred {

    public static void main(String[] args)  {
        Engine engine = new Engine(1024,768);
        engine.createGLContext(false, true);

        DrawComponentIf gBufferRenderer = new GBufferInputRenderer(
                "GBuffer"
        );

        //DrawComponentIf gBufferCompositor = new GBufferCompositor("compositor");

        DrawComponentIf gbufferDirLightRenderer = new GBufferDirLightRenderer("Direction");
        DrawComponentIf gbufferPointLightRenderer = new GBufferPointLightRenderer("Point",false);

        DrawComponentIf outputScreen = new OutputScreenFBO("screen", GBUFFER_FBO);

        List<String> debugDatums = new LinkedList<>();
        debugDatums.add(GBUFFER_POSITION);
        debugDatums.add(GBUFFER_NORMAL);
        debugDatums.add(GBUFFER_AMBIENT);
        debugDatums.add(GBUFFER_ALBEDOSPEC);
        DebugWindowsRenderer debugWindowsRenderer = new DebugWindowsRenderer("debugWindow", debugDatums);

        //engine.addDrawComponent(plainInputRenderer).addDrawComponent(outputScreen);
        engine.addDrawComponent(gBufferRenderer)
                //.addDrawComponent(gBufferCompositor)
                .addDrawComponent(gbufferDirLightRenderer)
                .addDrawComponent(gbufferPointLightRenderer)
                .addDrawComponent(outputScreen)
                .addDrawComponent(debugWindowsRenderer);
                //.addDrawComponent(outputScreen);

        if(!engine.initAllComponents())
            return ;
        debugWindowsRenderer.setAlpha(0.5f);

        World world = new World();
        ModelImporter nanoSuit = new ModelImporter();
        if(!nanoSuit.loadFromLocation("/home/stiffme/models/nanosuitRef/nanosuit.obj"))
            return ;

        Vector3f[] positions = new Vector3f[]   {
                new Vector3f(-3.0f,  -3.0f, -3.0f),
                new Vector3f( 0.0f,  -3.0f, -3.0f),
                new Vector3f( 3.0f,  -3.0f, -3.0f),
                new Vector3f(-3.0f,  -3.0f,  0.0f),
                new Vector3f( 0.0f,  -3.0f,  0.0f),
                new Vector3f( 3.0f,  -3.0f,  0.0f),
                new Vector3f(-3.0f,  -3.0f,  3.0f),
                new Vector3f( 0.0f,  -3.0f,  3.0f),
                new Vector3f( 3.0f,  -3.0f,  3.0f),
        };
        nanoSuit.setInstances(positions.length);
        for(int i = 0; i < positions.length; ++ i)  {
            nanoSuit.setPosition(i, positions[i]);
            nanoSuit.setScale(i,0.25f);
        }

        world.addObject(nanoSuit);

        int NR_LIGHT = 32;
        Random random = new Random();
        for(int i = 0; i < NR_LIGHT; ++i) {
            float xPos = random.nextFloat() * 6.0f - 3.0f;
            float yPos = random.nextFloat() * 6.0f - 4.0f;
            float zPos = random.nextFloat() * 6.0f - 3.0f;

            float r = random.nextFloat() * 0.5f + 0.5f;
            float g = random.nextFloat() * 0.5f + 0.5f;
            float b = random.nextFloat() * 0.5f + 0.5f;
            PointLight pointLight = new PointLight(
                    0.1f,0.1f,0.1f,
                    r,g,b,
                    r,g,b,
                    xPos,yPos,zPos,
                    1.0f,0.7f,1.8f
            );
            world.addPointLight(pointLight);
        }


        while(!engine.shouldCloseWindow())   {
            glfwPollEvents();
            engine.draw(world);
        }

        engine.release();
        world.release();
    }
}
