import com.esipeng.opengl.engine.base.DrawComponentBase;
import com.esipeng.opengl.engine.base.Engine;
import com.esipeng.opengl.engine.spi.DrawComponentIf;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static uk.org.lidalia.slf4jtest.LoggingEvent.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import static java.util.Arrays.asList;

public class EngineTest {

    private static final TestLogger engineLogger = TestLoggerFactory.getTestLogger(Engine.class);

    private DrawComponentBase phase1, phase2, phase3, phase2o;

    private class DummyDrawComponent extends DrawComponentBase {
        public DummyDrawComponent(String name, Set<String> input, Set<String> output)    {
            super(name, input,output);
        }

        @Override
        public boolean init(DrawContextIf context) {
            return true;
        }
    }

    @Before
    public void setUp(){
        phase1 = mock(DrawComponentBase.class);
        when(phase1.init(any())).thenReturn(true);
        when(phase1.getInputDatum()).thenReturn(new HashSet<>());
        when(phase1.getOutputDatum()).thenReturn(new HashSet<String>(){{
            add("phase1o");
        }});
        when(phase1.getName()).thenReturn("phase1");

        phase2 = mock(DrawComponentBase.class);
        when(phase2.init(any())).thenReturn(true);
        when(phase2.getInputDatum()).thenReturn(new HashSet<String>(){{
            add("phase1o");
        }});
        when(phase2.getOutputDatum()).thenReturn(new HashSet<String>(){{
            add("phase2oa");
            add("phase2ob");
        }});
        when(phase2.getName()).thenReturn("phase2");

        phase2o = mock(DrawComponentBase.class);
        when(phase2o.init(any())).thenReturn(true);
        when(phase2o.getInputDatum()).thenReturn(new HashSet<String>());
        when(phase2o.getOutputDatum()).thenReturn(new HashSet<String>(){{
            add("phase1o");
        }});
        when(phase2o.getName()).thenReturn("phase2o");


        phase3 = mock(DrawComponentBase.class);
        when(phase3.init(any())).thenReturn(true);
        when(phase3.getInputDatum()).thenReturn(new HashSet<String>(){{
            add("phase2oa");
            add("phase2ob");
        }});
        when(phase3.getOutputDatum()).thenReturn(new HashSet<String>());
        when(phase3.getName()).thenReturn("phase3");
    }



    @Test
    public void testSuccessfulChain()   {
        TestLoggerFactory.clear();
        Engine engine = new Engine(1024,1024);
        try{
            engine.createGLContext(false, false);
            engine.addDrawComponent(phase1).addDrawComponent(phase2).addDrawComponent(phase3);
            assertTrue(engine.initAllComponents());
            assertThat(engineLogger.getLoggingEvents(), is(new ArrayList<>()));
            Mockito.verify(phase1, Mockito.times(1)).init(any());
            Mockito.verify(phase2, Mockito.times(1)).init(any());
            Mockito.verify(phase3, Mockito.times(1)).init(any());

        } catch (Exception e)   {
            e.printStackTrace();
        } finally {
            engine.release();
        }
    }

    @Test
    public void testUnsuccessfulChain() {
        TestLoggerFactory.clear();
        Engine engine = new Engine(1024,1024);
        try{
            engine.createGLContext(false, false);
            engine.addDrawComponent(phase1).addDrawComponent(phase3);
            assertFalse(engine.initAllComponents());

            assertThat(engineLogger.getLoggingEvents(),
                    is(Collections.singletonList(error("{} needs input datum {}, not found", "phase3", "phase2oa"))));

        } catch (Exception e)   {
            e.printStackTrace();
        }finally {
            engine.release();
        }

    }

    @Test
    public void testOverwritingChain()  {
        TestLoggerFactory.clear();
        Engine engine = new Engine(1024,1024);
        try{
            engine.createGLContext(false, false);
            engine.addDrawComponent(phase1).addDrawComponent(phase2o);
            assertTrue(engine.initAllComponents());
            Mockito.verify(phase1, Mockito.times(1)).init(any());
            Mockito.verify(phase2o, Mockito.times(1)).init(any());

            assertThat(engineLogger.getLoggingEvents(), is(asList(
                    warn("{} overwrites datum {}", "phase2o", "phase1o")
            )));
        } catch (Exception e)   {
            e.printStackTrace();
        }finally {
            engine.release();
        }
    }

}
