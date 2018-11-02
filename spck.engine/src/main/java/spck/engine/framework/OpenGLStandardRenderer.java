package spck.engine.framework;

import org.lwjgl.opengl.GL41;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.Stats;
import spck.engine.ecs.ECS;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.render.Batch;
import spck.engine.render.LayoutQualifier;
import spck.engine.render.Renderer;
import spck.engine.render.textures.TextureUVModifier;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class OpenGLStandardRenderer implements Renderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGLStandardRenderer.class);
    private static final int INSTANCED_DATA_SIZE_IN_BYTES = 19; // transformationMatrixInstanced + uv scale + uv offset
    private static final List<Integer> vaos = new ArrayList<>();
    private static final List<Integer> vbos = new ArrayList<>();

    public OpenGLStandardRenderer() {
        MessageBus.register(LifeCycle.CLEANUP.eventID(), this::onCleanUp);
    }

    @Override
    public void uploadBatchDataToGPU(Batch batch) {
        LOGGER.trace("Uploading data to GPU for batch {}, num of Entities: {}", batch.getID(), batch.getNumOfEntities());
        GL.genVaoContext(vaoId -> {
            LOGGER.trace("    VAO created {}", vaoId);
            vaos.add(vaoId);
            batch.setVaoID(vaoId);

            loadBatchMeshDataIntoVAO(batch);
            setupInstancedRendering(batch);
            loadInstancedRenderingData(batch);

            batch.dataUpdated();
        });

        // don't unbind before unbinding VAO, because it's state is not saved
        // VBOs' state is saved because of the call on glVertexAttribPointer
        GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void updateBatchDataInGPU(Batch batch) {
        if (batch.getNumOfEntities() == 0) {
            LOGGER.trace("Batch {} is empty, removing its data from GPU", batch.getID());
            Stats.vboMemoryUsed -= batch.getOldSize() * batch.getEntityMemoryUsage();
            removeBatchDataFromGPU(batch);
            return;
        }

        GL.bufferContext(batch.getInstancedVboID(), () -> {
            LOGGER.trace("Updating batch {} data in GPU. Size: {}->{}",
                    batch.getID(),
                    batch.getOldSize(),
                    batch.getNumOfEntities()
            );

            if (batch.wasSizeChanged()) {
                // updating the storage size
                GL41.glBufferData(GL41.GL_ARRAY_BUFFER, batch.getNumOfEntities() * INSTANCED_DATA_SIZE_IN_BYTES * Float.BYTES, GL41.GL_DYNAMIC_DRAW);
            }

            Stats.vboMemoryUsed -= batch.getOldSize() * batch.getEntityMemoryUsage();
            float[] instancedVBOData = getInstancedVBOData(batch);
            // updating the data in the array buffer
            GL41.glBufferSubData(GL41.GL_ARRAY_BUFFER, 0, instancedVBOData);
            LOGGER.trace("    Data has been updated for batch {}", batch.getID());
        });

        batch.dataUpdated();
    }

    @Override
    public void render(Batch batch) {
        Stats.numOfVerts += batch.getMesh().getIndices().length;
        Stats.numOfTotalVerts += batch.getMesh().getIndices().length * batch.getNumOfEntities();

        GL.vaoContext(batch.getVaoID(), () -> {
            GL41.glEnableVertexAttribArray(LayoutQualifier.VX_POSITION.location);
            GL41.glEnableVertexAttribArray(LayoutQualifier.VX_NORMAL.location);

            if (batch.getMaterial().hasDiffuseTexture()) {
                GL41.glEnableVertexAttribArray(LayoutQualifier.VX_UV_COORDS.location);
                GL41.glEnableVertexAttribArray(LayoutQualifier.INS_UV_SCALE.location);
                GL41.glEnableVertexAttribArray(LayoutQualifier.INS_UV_OFFSET.location);
                GL41.glActiveTexture(batch.getMaterial().getDiffuseTexture().getGlTextureSlot());
                GL41.glBindTexture(GL41.GL_TEXTURE_2D, batch.getMaterial().getDiffuseTexture().getId());
            }

            // 4 slot for the transformation matrix, one for each column
            GL41.glEnableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL1.location);
            GL41.glEnableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL2.location);
            GL41.glEnableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL3.location);
            GL41.glEnableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL4.location);

            GL41.glDrawElementsInstanced(GL41.GL_TRIANGLES, batch.getMesh().getIndices().length, GL41.GL_UNSIGNED_INT, 0, batch.getNumOfEntities());

            GL41.glDisableVertexAttribArray(LayoutQualifier.VX_POSITION.location);
            GL41.glDisableVertexAttribArray(LayoutQualifier.VX_NORMAL.location);

            if (batch.getMaterial().hasDiffuseTexture()) {
                GL41.glDisableVertexAttribArray(LayoutQualifier.VX_UV_COORDS.location);
                GL41.glDisableVertexAttribArray(LayoutQualifier.INS_UV_SCALE.location);
                GL41.glDisableVertexAttribArray(LayoutQualifier.INS_UV_OFFSET.location);
                GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);
            }

            // 4 slot for the transformation matrix, one for each column
            GL41.glDisableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL1.location);
            GL41.glDisableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL2.location);
            GL41.glDisableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL3.location);
            GL41.glDisableVertexAttribArray(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL4.location);
        });
    }

    private void removeBatchDataFromGPU(Batch batch) {
        LOGGER.trace("Removing Batch {} data from GPU...", batch.getID());
        // Batch is now empty, delete datas
        GL41.glDeleteVertexArrays(batch.getVaoID());
        GL41.glDeleteBuffers(batch.getIndicesVBOId());
        GL41.glDeleteBuffers(batch.getNormalsVBOId());
        GL41.glDeleteBuffers(batch.getVerticesVBOId());
        GL41.glDeleteBuffers(batch.getInstancedVboID());
        vbos.remove(batch.getIndicesVBOId());
        vbos.remove(batch.getNormalsVBOId());
        vbos.remove(batch.getVerticesVBOId());
        vbos.remove(batch.getInstancedVboID());

        if (batch.getUvVBOId() != null) {
            GL41.glDeleteBuffers(batch.getUvVBOId());
            vbos.remove(batch.getUvVBOId());
        }

        vaos.remove(batch.getVaoID());
        LOGGER.trace("Batch {} data removed from GPU", batch.getID());
    }

    private void onCleanUp() {
        vaos.forEach(GL41::glDeleteVertexArrays);
        vbos.forEach(GL41::glDeleteBuffers);
    }

    private void setupInstancedRendering(Batch batch) {
        LOGGER.trace("Setting up instanced rendering for {} in [VAO:{}]", batch.getMesh(), batch.getVaoID());

        // Create VBO for instanced attributes
        int instancedDataVboId = GL41.glGenBuffers();
        vbos.add(instancedDataVboId);
        LOGGER.trace("    INSTANCED_DATA-VBO:{}", instancedDataVboId);
        batch.setInstancedVboID(instancedDataVboId);

        GL.bufferContext(instancedDataVboId, () -> {
            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, batch.getNumOfEntities() * INSTANCED_DATA_SIZE_IN_BYTES * Float.BYTES, GL41.GL_DYNAMIC_DRAW);

            // add transformationMatrix instanced attribute to VAO
            addInstancedVAOAttributeRequiresBind(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL1.location, 4, GL41.GL_FLOAT, 0);
            addInstancedVAOAttributeRequiresBind(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL2.location, 4, GL41.GL_FLOAT, 4);
            addInstancedVAOAttributeRequiresBind(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL3.location, 4, GL41.GL_FLOAT, 8);
            addInstancedVAOAttributeRequiresBind(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL4.location, 4, GL41.GL_FLOAT, 12);
            addInstancedVAOAttributeRequiresBind(LayoutQualifier.INS_UV_SCALE.location, 1, GL41.GL_FLOAT, 16);
            addInstancedVAOAttributeRequiresBind(LayoutQualifier.INS_UV_OFFSET.location, 2, GL41.GL_FLOAT, 17);
        });
    }

    private void loadInstancedRenderingData(Batch batch) {
        GL.bufferContext(batch.getInstancedVboID(), () -> {
            float[] instancedVBOData = getInstancedVBOData(batch);
            GL41.glBufferData(batch.getInstancedVboID(), instancedVBOData, GL41.GL_DYNAMIC_DRAW);
            GL41.glBufferSubData(GL41.GL_ARRAY_BUFFER, 0, instancedVBOData);
        });
    }

    private float[] getInstancedVBOData(Batch batch) {
        float[] vboData = new float[batch.getNumOfEntities() * INSTANCED_DATA_SIZE_IN_BYTES];
        int offset = 0;

        int index = 0;
        for (int entityId : batch.getEntities()) {
            RenderComponent component = ECS.world.getEntity(entityId).getComponent(RenderComponent.class);
            component.transform.getTransformationMatrix().get(vboData, offset);
            offset += 16;

            if (component.material.getDiffuseTextureUVModifier().isPresent()) {
                TextureUVModifier modifier = component.material.getDiffuseTextureUVModifier().get();
                vboData[offset++] = modifier.getScale();
                vboData[offset++] = modifier.getOffset().x;
                vboData[offset++] = modifier.getOffset().y;
            } else {
                vboData[offset++] = 0;
                vboData[offset++] = 0;
                vboData[offset++] = 0;
            }

            if (index == 0 && offset != INSTANCED_DATA_SIZE_IN_BYTES) {
                Stats.vboMemoryMisused = true;
            }

            index++;
        }

        batch.storeEntityMemoryUsage(offset / batch.getNumOfEntities());
        Stats.vboMemoryUsed += offset;
        return vboData;
    }

    private void loadBatchMeshDataIntoVAO(Batch batch) {
        LOGGER.trace("Loading mesh {} into [VAO:{}]", batch.getMesh(), batch.getVaoID());

        // vertices
        int verticesVboID = createAndStoreDataInVBO(batch.getMesh().getVertices());
        vbos.add(verticesVboID);
        LOGGER.trace("    VERTICES-VBO:{}", verticesVboID);
        batch.setVerticesVBOId(verticesVboID);
        addVAOAttribute(verticesVboID, LayoutQualifier.VX_POSITION.location, 3);

        // indices
        int indicesVboID = createAndStoreDataInVBO(batch.getMesh().getIndices());
        vbos.add(indicesVboID);
        LOGGER.trace("    INDICES-VBO:{}", indicesVboID);
        batch.setIndicesVBOId(indicesVboID);

        // normals
        int normalsVboID = createAndStoreDataInVBO(batch.getMesh().getNormals());
        vbos.add(normalsVboID);
        LOGGER.trace("    NORMALS-VBO:{}", normalsVboID);
        addVAOAttribute(normalsVboID, LayoutQualifier.VX_NORMAL.location, 3);
        batch.setNormalsVBOId(normalsVboID);

        // UV coords
        if (batch.getMaterial().hasDiffuseTexture()) {
            if (batch.getMesh().getUVCoords().length == 0) {
                throw new RuntimeException(String.format("Batch %s has diffuse texture, but no UV coords set for the mesh", batch.getID()));
            }

            int uvVboId = createAndStoreDataInVBO(batch.getMesh().getUVCoords());
            LOGGER.trace("    UV_COORDS-VBO:{}", uvVboId);
            addVAOAttribute(uvVboId, LayoutQualifier.VX_UV_COORDS.location, 2);
            batch.setUVVBOId(uvVboId);
        }
    }

    private static void addVAOAttribute(int vboId, int attributeIndex, int size) {
        GL.bufferContext(vboId, () -> GL41.glVertexAttribPointer(attributeIndex, size, GL41.GL_FLOAT, false, 0, 0));
    }

    private static int createAndStoreDataInVBO(float[] data) {
        return GL.genBufferContext(vboId -> {
            FloatBuffer buffer = (FloatBuffer) ((Buffer) MemoryUtil.memAllocFloat(data.length).put(data)).flip();
            vbos.add(vboId);

            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, buffer, GL41.GL_DYNAMIC_DRAW);
            MemoryUtil.memFree(buffer);
        });
    }

    private static int createAndStoreDataInVBO(int[] data) {
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

    private static void addInstancedVAOAttributeRequiresBind(int attributeIndex, int dataSize, int dataType, int offset) {
        GL41.glVertexAttribPointer(attributeIndex, dataSize, dataType, false, INSTANCED_DATA_SIZE_IN_BYTES * Float.BYTES, offset * Float.BYTES);
        GL41.glVertexAttribDivisor(attributeIndex, 1);
    }
}