package spck.engine.render.shader;

import org.joml.*;
import org.lwjgl.opengl.GL41;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.util.ResourceLoader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractShader {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractShader.class);
    private final static List<Integer> vertexShaders = new ArrayList<>();
    private final static List<Integer> fragmentShaders = new ArrayList<>();
    private final Map<String, Integer> uniforms = new HashMap<>();
    private final Class child;
    private int ID;

    public AbstractShader(Class child) {
        this.child = child;
    }

    public void init() {
        ID = GL41.glGenProgramPipelines();
        LOGGER.debug("Pipeline was created [ID:{}] for {}", ID, this.getClass().getSimpleName());

        MessageBus.register(LifeCycle.CLEANUP.eventID(), this::cleanUp);
    }

    protected void bind() {
        GL41.glBindProgramPipeline(ID);
    }

    protected void unbind() {
        GL41.glBindProgramPipeline(0);
    }

    protected void createUniform(int programID, Enum uniformName) {
        createUniform(programID, uniformName.name());
    }

    protected void createUniform(int programID, String uniformName) {
        if (uniforms.containsKey(uniformName)) {
            LOGGER.error("Uniform [NAME:{}] is already created", uniformName);
            return;
        }

        int location = GL41.glGetUniformLocation(programID, uniformName);
        if (location < 0) {
            LOGGER.error("Uniform could not find ({}) or not used, so optimized out when trying to create: {}", location, uniformName);
        } else {
            LOGGER.trace("Uniform [NAME:{}] created for program [ID:{}] in pipeline [ID:{}]", uniformName, programID, ID);
        }

        uniforms.put(uniformName, location);
    }

    protected int attachVertexShader(String path) {
        int vertexID = GL41.glCreateShaderProgramv(GL41.GL_VERTEX_SHADER, ResourceLoader.load(path));
        GL41.glUseProgramStages(ID, GL41.GL_VERTEX_SHADER_BIT, vertexID);
        validateShaderProgram(vertexID, path);
        vertexShaders.add(vertexID);
        return vertexID;
    }

    protected int attachFragmentShader(String path) {
        int fragmentID = GL41.glCreateShaderProgramv(GL41.GL_FRAGMENT_SHADER, ResourceLoader.load(path));
        GL41.glUseProgramStages(ID, GL41.GL_FRAGMENT_SHADER_BIT, fragmentID);
        validateShaderProgram(fragmentID, path);
        fragmentShaders.add(fragmentID);
        return fragmentID;
    }

    protected void setUniform(int programID, Enum uniformName, Matrix4f value) {
        checkUniform(uniformName.name());
        setUniform(programID, uniformName.name(), value);
    }

    protected void setUniform(int programID, String uniformName, Matrix4f value) {
        checkUniform(uniformName);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);
            GL41.glProgramUniformMatrix4fv(programID, uniforms.get(uniformName), false, buffer);
        }
    }

    protected void setUniform(int programID, String uniformName, Vector2f value) {
        checkUniform(uniformName);
        GL41.glProgramUniform2f(programID, uniforms.get(uniformName), value.x, value.y);
    }

    protected void setUniform(int programID, String uniformName, Vector4f value) {
        checkUniform(uniformName);
        GL41.glProgramUniform4f(programID, uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    protected void setUniform(int programID, String uniformName, int value) {
        checkUniform(uniformName);
        GL41.glProgramUniform1i(programID, uniforms.get(uniformName), value);
    }

    protected void setUniform(int programID, ShaderUniform uniformName, int value) {
        checkUniform(uniformName.name());
        GL41.glProgramUniform1i(programID, uniforms.get(uniformName.name()), value);
    }

    protected void setUniform(int programID, String uniformName, float value) {
        checkUniform(uniformName);
        GL41.glProgramUniform1f(programID, uniforms.get(uniformName), value);
    }

    protected void setUniform(int programID, String uniformName, Vector3f value) {
        checkUniform(uniformName);
        GL41.glProgramUniform3f(programID, uniforms.get(uniformName), value.x, value.y, value.z);
    }

    protected void setUniform(int programID, Enum uniform, Vector3f value) {
        checkUniform(uniform.name());
        GL41.glProgramUniform3f(programID, uniforms.get(uniform.name()), value.x, value.y, value.z);
    }

    protected void setUniform(int programID, String uniformName, boolean value) {
        checkUniform(uniformName);
        GL41.glProgramUniform1i(programID, uniforms.get(uniformName), value ? 1 : 0);
    }

    protected void setUniform(int programID, String uniformName, Vector3i value) {
        checkUniform(uniformName);
        GL41.glProgramUniform3i(programID, uniforms.get(uniformName), value.x, value.y, value.z);
    }

    private void cleanUp() {
        LOGGER.debug("Cleaning up {}...", child.getSimpleName());
        unbind();

        GL41.glDeleteProgramPipelines(ID);
        vertexShaders.forEach(GL41::glDeleteProgram);
        fragmentShaders.forEach(GL41::glDeleteProgram);
    }

    private void checkUniform(String uniformName) {
        if (!uniforms.containsKey(uniformName)) {
            throw new RuntimeException(String.format("Uniform %s was not created", uniformName));
        }
    }

    private void validateShaderProgram(int programID, String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(1);
            GL41.glGetProgramiv(programID, GL41.GL_LINK_STATUS, buffer);
            if (buffer.get() == GL41.GL_FALSE) {
                LOGGER.error(
                        "Shader '{}' could not be linked\n---\n{}---",
                        path,
                        GL41.glGetProgramInfoLog(programID, 1024)
                );
                throw new RuntimeException("Shader validating failed");
            }
        }
    }
}