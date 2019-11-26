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

class OpenGLAbstractGPUDataStore {
    final List<Integer> vaos = new ArrayList<>();
    final List<Integer> vbos = new ArrayList<>();
    private final int instancedDataSize;

    OpenGLAbstractGPUDataStore(int instancedDataSize) {
        this.instancedDataSize = instancedDataSize;
        MessageBus.register(LifeCycle.CLEANUP.eventID(), this::onCleanUp);
    }

    private void onCleanUp() {
        vaos.forEach(GL41::glDeleteVertexArrays);
        vbos.forEach(GL41::glDeleteBuffers);
    }

    void addVAOAttribute(int vboId, int attributeIndex, int size) {
        GL.bufferContext(vboId, () -> GL41.glVertexAttribPointer(attributeIndex, size, GL41.GL_FLOAT, false, 0, 0));
    }

    int createAndStoreDataInVBO(float[] data) {
        return GL.genBufferContext(vboId -> {
            FloatBuffer buffer = (FloatBuffer) ((Buffer) MemoryUtil.memAllocFloat(data.length).put(data)).flip();
            vbos.add(vboId);

            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, buffer, GL41.GL_DYNAMIC_DRAW);
            MemoryUtil.memFree(buffer);
        });
    }

    int createAndStoreDataInVBO(int[] data) {
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

    void addInstancedVAOAttributeRequiresBind(int attributeIndex, int dataSize, int dataType, int offset) {
        GL41.glVertexAttribPointer(attributeIndex, dataSize, dataType, false, instancedDataSize * Float.BYTES, offset * Float.BYTES);
        GL41.glVertexAttribDivisor(attributeIndex, 1);
    }
}
