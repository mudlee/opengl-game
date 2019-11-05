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
import spck.engine.render.GPUDataStore;
import spck.engine.render.LayoutQualifier;
import spck.engine.render.Material;
import spck.engine.render.MeshMaterialBatch;
import spck.engine.render.textures.TextureUVModifier;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenGLDefaultGPUDataStore implements GPUDataStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGLDefaultGPUDataStore.class);
    // transformationMatrixInstanced(4x4) + uv scale(1) + uv offset(2)
    private static final int INSTANCED_DATA_SIZE_IN_BYTES = 19;
    // transformationMatrixInstanced(4x4)
    private static final int INSTANCED_DATA_AABB_SIZE_IN_BYTES = 16;
    private static final List<Integer> vaos = new ArrayList<>();
    private static final List<Integer> vbos = new ArrayList<>();

    public OpenGLDefaultGPUDataStore() {
        MessageBus.register(LifeCycle.CLEANUP.eventID(), this::onCleanUp);
    }

    @Override
    public void uploadBatchDataToGPU(MeshMaterialBatch batch) {
        LOGGER.trace("Uploading data to GPU for batch {}, num of Entities: {}", batch.getID(), batch.getNumOfEntities());
        GL.genVaoContext(vaoId -> {
            LOGGER.trace("VAO created {}", vaoId);
            vaos.add(vaoId);
            batch.setVaoID(vaoId);

            loadBatchMeshDataIntoVAO(batch);
            setupInstancedRendering(batch);
            loadInstancedRenderingData(batch);
        }, () -> GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, 0));

        GL.genVaoContext(vaoId -> {
            LOGGER.trace("AABB VAO created {}", vaoId);
            vaos.add(vaoId);
            batch.setAABBVaoID(vaoId);

            loadAABBBatchMeshDataIntoVAO(batch);
            setupAABBInstancedRendering(batch);
            loadAABBInstancedRenderingData(batch);
        }, () -> GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, 0));
        batch.dataUpdated();
    }

    @Override
    public void updateBatchDataInGPU(MeshMaterialBatch batch) {
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

        GL.bufferContext(batch.getAABBInstancedDataVboID(), () -> {
            LOGGER.trace("Updating batch {} AABB data in GPU. Size: {}->{}",
                    batch.getID(),
                    batch.getOldSize(),
                    batch.getNumOfEntities()
            );

            if (batch.wasSizeChanged()) {
                // updating the storage size
                GL41.glBufferData(GL41.GL_ARRAY_BUFFER, batch.getNumOfEntities() * INSTANCED_DATA_AABB_SIZE_IN_BYTES * Float.BYTES, GL41.GL_DYNAMIC_DRAW);
            }

            Stats.vboMemoryUsed -= batch.getOldSize() * batch.getEntityMemoryUsage();
            float[] instancedVBOData = getAABBInstancedVBOData(batch);
            // updating the data in the array buffer
            GL41.glBufferSubData(GL41.GL_ARRAY_BUFFER, 0, instancedVBOData);
            LOGGER.trace("    AABB Data has been updated for batch {}", batch.getID());
        });

        batch.dataUpdated();
    }

    private void removeBatchDataFromGPU(MeshMaterialBatch batch) {
        LOGGER.trace("Removing Batch {} data from GPU...", batch.getID());
        // Batch is now empty, delete datas
        GL41.glDeleteVertexArrays(batch.getVaoID());
        GL41.glDeleteBuffers(batch.getIndicesVBOID());
        GL41.glDeleteBuffers(batch.getNormalsVBOID());
        GL41.glDeleteBuffers(batch.getVerticesVBOID());
        GL41.glDeleteBuffers(batch.getInstancedVboID());
        GL41.glDeleteBuffers(batch.getAABBVerticesVboID());
        GL41.glDeleteBuffers(batch.getAABBIndicesVboID());
        GL41.glDeleteBuffers(batch.getAABBInstancedDataVboID());
        vbos.remove(batch.getIndicesVBOID());
        vbos.remove(batch.getNormalsVBOID());
        vbos.remove(batch.getVerticesVBOID());
        vbos.remove(batch.getInstancedVboID());
        vbos.remove(batch.getAABBVerticesVboID());
        vbos.remove(batch.getAABBIndicesVboID());
        vbos.remove(batch.getAABBInstancedDataVboID());

        if (batch.getUvVBOId() != null) {
            GL41.glDeleteBuffers(batch.getUvVBOId());
            vbos.remove(batch.getUvVBOId());
        }

        vaos.remove(batch.getVaoID());
        vaos.remove(batch.getAABBVaoID());
        LOGGER.trace("Batch {} data removed from GPU", batch.getID());
    }

    private void onCleanUp() {
        vaos.forEach(GL41::glDeleteVertexArrays);
        vbos.forEach(GL41::glDeleteBuffers);
    }

    private void setupInstancedRendering(MeshMaterialBatch batch) {
        LOGGER.trace("    Setting up instanced rendering for {} in [VAO:{}]", batch.getMesh(), batch.getVaoID());

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

    private void setupAABBInstancedRendering(MeshMaterialBatch batch) {
        LOGGER.trace("    Setting up AABB instanced rendering for {} in [VAO:{}]", batch.getMesh(), batch.getAABBVaoID());

        // Create VBO for instanced attributes
        int instancedDataVboId = GL41.glGenBuffers();
        vbos.add(instancedDataVboId);
        LOGGER.trace("    AABB_INSTANCED_DATA-VBO:{}", instancedDataVboId);
        batch.setAABBInstancedVboID(instancedDataVboId);

        GL.bufferContext(instancedDataVboId, () -> {
            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, batch.getNumOfEntities() * INSTANCED_DATA_AABB_SIZE_IN_BYTES * Float.BYTES, GL41.GL_DYNAMIC_DRAW);

            // add transformationMatrix instanced attribute to VAO
            addInstancedVAOAttributeRequiresBind(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL1.location, 4, GL41.GL_FLOAT, 0);
            addInstancedVAOAttributeRequiresBind(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL2.location, 4, GL41.GL_FLOAT, 4);
            addInstancedVAOAttributeRequiresBind(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL3.location, 4, GL41.GL_FLOAT, 8);
            addInstancedVAOAttributeRequiresBind(LayoutQualifier.INS_TRANSFORMATION_MATRIX_COL4.location, 4, GL41.GL_FLOAT, 12);
        });
    }

    private void loadInstancedRenderingData(MeshMaterialBatch batch) {
        GL.bufferContext(batch.getInstancedVboID(), () -> {
            float[] instancedVBOData = getInstancedVBOData(batch);
            GL41.glBufferData(batch.getInstancedVboID(), instancedVBOData, GL41.GL_DYNAMIC_DRAW);
            GL41.glBufferSubData(GL41.GL_ARRAY_BUFFER, 0, instancedVBOData);
        });
    }

    private void loadAABBInstancedRenderingData(MeshMaterialBatch batch) {
        GL.bufferContext(batch.getAABBInstancedDataVboID(), () -> {
            float[] instancedVBOData = getAABBInstancedVBOData(batch);
            GL41.glBufferData(batch.getAABBInstancedDataVboID(), instancedVBOData, GL41.GL_DYNAMIC_DRAW);
            GL41.glBufferSubData(GL41.GL_ARRAY_BUFFER, 0, instancedVBOData);
        });
    }

    private float[] getInstancedVBOData(MeshMaterialBatch batch) {
        float[] vboData = new float[batch.getNumOfEntities() * INSTANCED_DATA_SIZE_IN_BYTES];
        int offset = 0;

        int index = 0;
        for (int entityId : batch.getEntities()) {
            RenderComponent component = ECS.world.getEntity(entityId).getComponent(RenderComponent.class);
            component.transform.getTransformationMatrix().get(vboData, offset);
            offset += 16;

            Supplier<RuntimeException> exceptionSupplier = () -> new RuntimeException("Material not found");
            Material componentMaterial = component.meshMaterialCollection.findMaterial(batch.getMaterial()).orElseThrow(exceptionSupplier);

            if (componentMaterial.getDiffuseTextureUVModifier().isPresent()) {
                TextureUVModifier modifier = componentMaterial.getDiffuseTextureUVModifier().get();
                vboData[offset++] = modifier.getScale();
                vboData[offset++] = modifier.getOffset().x;
                vboData[offset++] = modifier.getOffset().y;
            } else {
                vboData[offset++] = 1;
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

    private float[] getAABBInstancedVBOData(MeshMaterialBatch batch) {
        float[] vboData = new float[batch.getNumOfEntities() * INSTANCED_DATA_AABB_SIZE_IN_BYTES];
        int offset = 0;

        int index = 0;
        for (int entityId : batch.getEntities()) {
            RenderComponent component = ECS.world.getEntity(entityId).getComponent(RenderComponent.class);
            component.transform.getTransformationMatrix().get(vboData, offset);
            offset += 16;

            if (index == 0 && offset != INSTANCED_DATA_AABB_SIZE_IN_BYTES) {
                Stats.vboMemoryMisused = true;
            }

            index++;
        }

        batch.storeEntityMemoryUsage(batch.getEntityMemoryUsage() + offset / batch.getNumOfEntities());
        Stats.vboMemoryUsed += offset;
        return vboData;
    }

    private void loadBatchMeshDataIntoVAO(MeshMaterialBatch batch) {
        LOGGER.trace("    Loading mesh {} into [VAO:{}]", batch.getMesh(), batch.getVaoID());

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
        batch.setIndicesVBOID(indicesVboID);

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

    private void loadAABBBatchMeshDataIntoVAO(MeshMaterialBatch batch) {
        LOGGER.trace("    Loading AABB data {} into [VAO:{}]", batch.getMesh(), batch.getAABBVaoID());

        // AABB vertices
        int aabbVboID = createAndStoreDataInVBO(batch.getMesh().getAABBVertices());
        vbos.add(aabbVboID);
        LOGGER.trace("    AABB-VBO:{}", aabbVboID);
        addVAOAttribute(aabbVboID, LayoutQualifier.AABB_VX_POSITION.location, 3);
        batch.setAABBVBOID(aabbVboID);

        // AABB indices
        int aabbIndicesVboID = createAndStoreDataInVBO(batch.getMesh().getAABBIndices());
        vbos.add(aabbIndicesVboID);
        LOGGER.trace("    AABB_INDICES-VBO:{}", aabbIndicesVboID);
        batch.setAABBIndicesVBOID(aabbIndicesVboID);
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