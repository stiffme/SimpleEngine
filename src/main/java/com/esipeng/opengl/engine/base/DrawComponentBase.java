package com.esipeng.opengl.engine.base;

import com.esipeng.opengl.engine.spi.DrawComponentIf;
import com.esipeng.opengl.engine.spi.DrawContextIf;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import static org.lwjgl.opengl.GL33.*;

public class DrawComponentBase implements DrawComponentIf {
    private Set<String> inputDatum, outputDatum;
    private String name;

    protected DrawComponentBase(
            String name,
            Set<String> inputDatum,
            Set<String> outputDatum
    )   {
        this.inputDatum = inputDatum;
        this.outputDatum = outputDatum;
        this.name = name;
    }

    public boolean init() {
        return false;
    }

    public void beforeDraw(DrawContextIf context) {

    }

    public void draw(DrawContextIf context) {

    }

    public void afterDraw(DrawContextIf context) {

    }

    public void release(DrawContextIf context) {

    }

    public Set<String> getInputDatum() {
        return inputDatum;
    }

    public Set<String> getOutputDatum() {
        return outputDatum;
    }

    public String getName() {
        return name;
    }

    protected int linkProgram(int vShader, int fShader)    {
        int program = glCreateProgram();
        if(program == 0)
            return 0;

        glAttachShader(program, vShader);
        glAttachShader(program, fShader);
        glLinkProgram(program);

        int linkStatus = glGetProgrami(program, GL_LINK_STATUS);
        if(linkStatus != GL_TRUE)   {
            System.out.println("Link failed " +
                    glGetProgramInfoLog(program));
            return 0;
        }
        return program;
    }

    protected int linkProgram(int vShader,int gShader, int fShader)    {
        int program = glCreateProgram();
        if(program == 0)
            return 0;

        glAttachShader(program, vShader);
        glAttachShader(program, fShader);
        glAttachShader(program, gShader);
        glLinkProgram(program);

        int linkStatus = glGetProgrami(program, GL_LINK_STATUS);
        if(linkStatus != GL_TRUE)   {
            System.out.println("Link failed " +
                    glGetProgramInfoLog(program));
            return 0;
        }
        return program;
    }

    protected int loadShader(int type, String shaderSrc) throws Exception    {
        int shader = glCreateShader(type);
        glShaderSource(shader,shaderSrc);
        glCompileShader(shader);

        int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
        if(compiled != GL_TRUE) {
            System.out.println("Failed to compile shader! \n" + shaderSrc );
            System.out.println(glGetShaderInfoLog(shader));
            throw new Exception("Failed to compile shader");
        }
        return shader;

    }

    protected int compileAndLinkProgram(String vShaderPath, String fShaderPath) throws Exception    {
        String vShaderSrc = loadFileFromResource(vShaderPath);
        String fShaderSrc = loadFileFromResource(fShaderPath);
        int vShader = loadShader(GL_VERTEX_SHADER, vShaderSrc);
        int fShader = loadShader(GL_FRAGMENT_SHADER, fShaderSrc);
        int program = linkProgram(vShader, fShader);
        return program;
    }

    protected int compileAndLinkProgram(String vShaderPath,String gShaderPath, String fShaderPath) throws Exception    {
        String vShaderSrc = loadFileFromResource(vShaderPath);
        String fShaderSrc = loadFileFromResource(fShaderPath);
        String gShaderSrc = loadFileFromResource(gShaderPath);

        int vShader = loadShader(GL_VERTEX_SHADER, vShaderSrc);
        int fShader = loadShader(GL_FRAGMENT_SHADER, fShaderSrc);
        int gShader = loadShader(GL_GEOMETRY_SHADER, gShaderSrc);
        int program = linkProgram(vShader,gShader, fShader);
        return program;
    }

    protected String loadFileFromResource(String resource ) throws Exception {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resource).toURI())));
    }

    protected String getResourcePath(String resource) throws Exception  {
        return Paths.get(getClass().getClassLoader().getResource(resource).toURI()).toAbsolutePath().toString();
    }

}
