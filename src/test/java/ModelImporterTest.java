import com.esipeng.opengl.engine.base.Engine;
import com.esipeng.opengl.engine.base.World;
import com.esipeng.opengl.engine.importer.ModelImporter;
import com.esipeng.opengl.engine.shader.OutputScreen;
import com.esipeng.opengl.engine.shader.PlainInputRenderer;
import com.esipeng.opengl.engine.spi.DrawComponentIf;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;

public class ModelImporterTest {
    private static final int NUM_INSTANCE = 100;

    public static void main(String[] args) {
        Engine engine = new Engine(1024, 768);
        engine.createGLContext(false, true);

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
        ModelImporter nanosuit = new ModelImporter(NUM_INSTANCE);
        if(!nanosuit.loadFromResource("model/nanosuit/nanosuit.obj"))
            return ;

        int lenth = (int)Math.sqrt(NUM_INSTANCE);
        float gap = 10.f / lenth;
        for(int i = 0; i < NUM_INSTANCE; ++i)   {
            int line = i % lenth;
            int col = i / lenth;
            nanosuit.setScale(i, 0.1f);
            nanosuit.setPosition(i, -5.f + gap * line, -5f + gap * col, 0.0f);
            nanosuit.setRotate(i, (float)Math.toRadians(i),1.0f,1.0f,1.0f);
        }

        world.addObject(nanosuit);
        //engine.enableFpsView();
        engine.enableProfiling();
        while(!engine.shouldCloseWindow())   {
            glfwPollEvents();

            for(int i = 0; i < NUM_INSTANCE; ++i)   {
                nanosuit.setRotate(i, (float)Math.toRadians(i + glfwGetTime() * 200),1.0f,1.0f,1.0f);
            }

            engine.draw(world);
        }

        engine.release();
        world.release();
    }
}
