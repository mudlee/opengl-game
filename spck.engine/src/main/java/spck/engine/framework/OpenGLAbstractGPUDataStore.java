package spck.engine.framework;

import org.lwjgl.opengl.GL41;
import org.lwjgl.system.MemoryUtil;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class OpenGLAbstractGPUDataStore {
    final List<Integer> vaos = new ArrayList<>();
    final List<Integer> vbos = new ArrayList<>();
    private final int instancedDataSize;

    public OpenGLAbstractGPUDataStore(int instancedDataSize) {
        this.instancedDataSize = instancedDataSize;
        MessageBus.register(LifeCycle.CLEANUP.eventID(), this::onCleanUp);
    }

    private void onCleanUp() {
        vaos.forEach(GL41::glDeleteVertexArrays);
        vbos.forEach(GL41::glDeleteBuffers);
    }

    public void addVAOAttribute(int vboId, int attributeIndex, int size) {
        addVAOAttribute(vboId, attributeIndex, size, GL41.GL_FLOAT, false, 0, 0);
    }

    public void addVAOAttribute(int vboId, int attributeIndex, int size, int type, boolean normalized, int stride, int pointer) {
        GL.bufferContext(vboId, () -> GL41.glVertexAttribPointer(attributeIndex, size, type, normalized, stride, pointer));
    }

    public int createVBO(){
        int vboId = GL41.glGenBuffers();
        vbos.add(vboId);
        return vboId;
    }

    public int createAndStoreDataInVBO(float[] data) {
        return GL.genBufferContext(vboId -> {
            FloatBuffer buffer = (FloatBuffer) ((Buffer) MemoryUtil.memAllocFloat(data.length).put(data)).flip();
            vbos.add(vboId);

            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, buffer, GL41.GL_DYNAMIC_DRAW);
            MemoryUtil.memFree(buffer);
        });
    }

    public int createAndStoreDataInVBO(int[] data) {
        int vboId = GL41.glGenBuffers();
        vbos.add(vboId);

        IntBuffer buffer = (IntBuffer) ((Buffer) MemoryUtil.memAllocInt(data.length).put(data)).flip();

        GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, vboId);
        GL41.glBufferData(GL41.GL_ELEMENT_ARRAY_BUFFER, buffer, GL41.GL_DYNAMIC_DRAW);
        MemoryUtil.memFree(buffer);
        // don't unbind before unbinding VAO, because it's state is not saved
        // VBOs' state is saved because of the call on glVertexAttribPointer

        return vboId;
    }

    public void addInstancedVAOAttributeRequiresBind(int attributeIndex, int dataSize, int dataType, int offset) {
        GL41.glVertexAttribPointer(attributeIndex, dataSize, dataType, false, instancedDataSize * Float.BYTES, offset * Float.BYTES);
        GL41.glVertexAttribDivisor(attributeIndex, 1);
    }
}
