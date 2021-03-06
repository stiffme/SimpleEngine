import com.esipeng.opengl.engine.base.Engine;
import com.esipeng.opengl.engine.base.World;
import com.esipeng.opengl.engine.spi.DrawComponentIf;
import com.esipeng.opengl.engine.shader.*;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

public class SingleColorQuardTest {

    //@Ignore
    @Test
    public void testSingleColorQuard() throws Exception   {
        Engine engine = new Engine(1024,768);
        try {
            engine.createGLContext(false, false);
            //1, single color quard to FBO
            DrawComponentIf singleColor =
                    new SingleColorQuard("SingleColor",
                            "screen",
                            0.0f,0.0f,1.0f);

            //2, output to the screen
            DrawComponentIf outputScreen =
                    new OutputScreen("outputScreen","screen");

            engine.addDrawComponent(singleColor);
            engine.addDrawComponent(outputScreen);
            assertTrue(engine.initAllComponents());

            engine.draw(new World());
        } catch (Exception e)   {
            e.printStackTrace();
        } finally {
            engine.release();
        }



    }
}
