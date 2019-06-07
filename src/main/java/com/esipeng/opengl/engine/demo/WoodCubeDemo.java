package com.esipeng.opengl.engine.demo;

import com.esipeng.opengl.engine.base.Engine;
import com.esipeng.opengl.engine.base.World;
import com.esipeng.opengl.engine.importer.WoodCube;
import com.esipeng.opengl.engine.shader.OutputScreen;
import com.esipeng.opengl.engine.shader.PlainInputRenderer;
import com.esipeng.opengl.engine.spi.DrawComponentIf;
import com.esipeng.opengl.engine.spi.DrawableObjectIf;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;

public class WoodCubeDemo {

    public static void main(String[] args)  {
        Engine engine = new Engine(1024,768);
        engine.createGLContext(true, true);

        //1, plain input renderer
        DrawComponentIf plainInputRenderer = new PlainInputRenderer(
                "Plain","world"
        );

        //2, output to screen
        DrawComponentIf outputScreen = new OutputScreen(
                "screen","world"
        );
        engine.addDrawComponent(plainInputRenderer)
                .addDrawComponent(outputScreen);

        if(!engine.initAllComponents())
            return ;

        World world = new World();
        DrawableObjectIf woodCube = WoodCube.getInstance();
        world.addObject(woodCube);
        engine.enableFpsView();

        while(!engine.shouldCloseWindow())   {
            glfwPollEvents();
            engine.draw(world);
        }

        engine.release();
        world.release();
    }
}
