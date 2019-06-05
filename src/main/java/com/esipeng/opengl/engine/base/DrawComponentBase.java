package com.esipeng.opengl.engine.base;

import com.esipeng.opengl.engine.spi.DrawComponentIf;
import com.esipeng.opengl.engine.spi.DrawContextIf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL33.*;

public class DrawComponentBase implements DrawComponentIf {
    private static final Logger logger = LoggerFactory.getLogger(DrawComponentBase.class);
    private Set<String> inputDatum, outputDatum;
    private String name;
    private LinkedHashSet<Integer> m_vbos, m_vaos, m_shaders,
            m_programs, m_textures, m_framebuffers, m_renderBuffer;

    protected DrawComponentBase(
            String name,
            Set<String> inputDatum,
            Set<String> outputDatum
    )   {
        this.inputDatum = inputDatum;
        this.outputDatum = outputDatum;
        this.name = name;

        m_vaos = new LinkedHashSet<>();
        m_vbos = new LinkedHashSet<>();
        m_shaders = new LinkedHashSet<>();
        m_programs = new LinkedHashSet<>();
        m_textures = new LinkedHashSet<>();
        m_framebuffers = new LinkedHashSet<>();
        m_renderBuffer = new LinkedHashSet<>();
    }

    public boolean init(DrawContextIf context) {
        return false;
    }

    public void beforeDraw(DrawContextIf context) {

    }

    public void draw(DrawContextIf context) {

    }

    public void afterDraw(DrawContextIf context) {

    }

    public void release(DrawContextIf context) {
        m_programs.forEach(GL33::glDeleteProgram);
        m_shaders.forEach(GL33::glDeleteShader);
        m_vbos.forEach(GL33::glDeleteBuffers);
        m_vaos.forEach(GL33::glDeleteVertexArrays);
        m_textures.forEach(GL33::glDeleteTextures);
        m_framebuffers.forEach(GL33::glDeleteFramebuffers);
        m_renderBuffer.forEach(GL33::glDeleteRenderbuffers);
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
        m_programs.add(program);
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
        m_programs.add(program);
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
        m_shaders.add(shader);
        return shader;

    }

    protected String loadFileFromResource(String resource ) throws Exception {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resource).toURI())));
    }

    protected byte[] loadBinaryFileFromResource(String resource ) throws Exception {
        return Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resource).toURI()));
    }

    protected String getResourcePath(String resource) throws Exception  {
        return Paths.get(getClass().getClassLoader().getResource(resource).toURI()).toAbsolutePath().toString();
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

    protected int getManagedRenderBuffer()  {
        int rbo = glGenRenderbuffers();
        m_renderBuffer.add(rbo);
        return rbo;
    }

    protected int getManagedVBO()  {
        int vbo = glGenBuffers();
        m_vbos.add(vbo);
        return vbo;
    }

    protected int getManagedVAO()  {
        int vao = glGenVertexArrays();
        m_vaos.add(vao);
        return vao;
    }

    protected int getManagedTexture()   {
        int texture = glGenTextures();
        m_textures.add(texture);
        return texture;
    }

    protected int getManagedFramebuffer()   {
        int framebuffer = glGenFramebuffers();
        m_framebuffers.add(framebuffer);
        return framebuffer;
    }

    //convinient function
    protected boolean setUniform1i(int program, String uniform, int value)   {
        int uniformLoc = glGetUniformLocation(program,uniform);
        if(uniformLoc == -1)    {
            logger.warn("Uniform {} not set for {}", uniform, value);
            return false;
        }
        glUseProgram(program);
        glUniform1i(uniformLoc, value);
        return true;
    }

    protected boolean setUniform1f(int program, String uniform, float value)   {
        int uniformLoc = glGetUniformLocation(program,uniform);
        if(uniformLoc == -1)    {
            logger.warn("Uniform {} not set for {}", uniform, value);
            return false;
        }
        glUseProgram(program);
        glUniform1f(uniformLoc, value);
        return true;
    }

    protected boolean setUniform3f(int program, String uniform, float v1, float v2, float v3)   {
        int uniformLoc = glGetUniformLocation(program,uniform);
        if(uniformLoc == -1)    {
            logger.warn("Uniform {} not set for {} {} {}", uniform, v1, v2, v3);
            return false;
        }
        glUseProgram(program);
        glUniform3f(uniformLoc, v1, v2, v3);
        return true;
    }

    protected boolean setUniform3f(int program, String uniform, Vector3f data)   {
        return setUniform3f(program,uniform, data.x, data.y, data.z);
    }

    protected boolean setUniformMatrix4(int program, String uniform, float[] data)   {
        int uniformLoc = glGetUniformLocation(program,uniform);
        if(uniformLoc == -1)    {
            logger.warn("Uniform {} not set ", uniform);
            return false;
        }
        glUseProgram(program);
        glUniformMatrix4fv(uniformLoc,false, data);
        return true;
    }

}
