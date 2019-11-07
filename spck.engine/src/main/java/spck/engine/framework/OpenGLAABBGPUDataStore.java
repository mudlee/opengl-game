package spck.engine.framework;

import org.lwjgl.opengl.GL41;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.debug.Stats;
import spck.engine.ecs.ECS;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.render.GPUDataStore;
import spck.engine.render.MeshMaterialBatch;

public class OpenGLAABBGPUDataStore extends AbstractGPUDataStore implements GPUDataStore {
    public enum LayoutQualifier {
        VX_POSITION(0),
        INS_TRANSFORMATION_MATRIX_COL1(1),
        INS_TRANSFORMATION_MATRIX_COL2(2),
        INS_TRANSFORMATION_MATRIX_COL3(3),
        INS_TRANSFORMATION_MATRIX_COL4(4);

        public int location;

        LayoutQualifier(int location) {
            this.location = location;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGLAABBGPUDataStore.class);
    // transformationMatrixInstanced(4x4)
    private static final int INSTANCED_DATA_AABB_SIZE_IN_BYTES = 16;

    public OpenGLAABBGPUDataStore() {
        super(INSTANCED_DATA_AABB_SIZE_IN_BYTES);
    }

    @Override
    public void uploadBatchDataToGPU(MeshMaterialBatch batch) {
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
    }

    private void removeBatchDataFromGPU(MeshMaterialBatch batch) {
        LOGGER.trace("Removing Batch {} AABB data from GPU...", batch.getID());
        // Batch is now empty, delete datas
        GL41.glDeleteVertexArrays(batch.getAABBVaoID());
        GL41.glDeleteBuffers(batch.getAABBVerticesVboID());
        GL41.glDeleteBuffers(batch.getAABBIndicesVboID());
        GL41.glDeleteBuffers(batch.getAABBInstancedDataVboID());
        vbos.remove(batch.getAABBVerticesVboID());
        vbos.remove(batch.getAABBIndicesVboID());
        vbos.remove(batch.getAABBInstancedDataVboID());

        vaos.remove(batch.getAABBVaoID());
        LOGGER.trace("Batch {} AABB data removed from GPU", batch.getID());
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

    private void loadAABBInstancedRenderingData(MeshMaterialBatch batch) {
        GL.bufferContext(batch.getAABBInstancedDataVboID(), () -> {
            float[] instancedVBOData = getAABBInstancedVBOData(batch);
            GL41.glBufferData(batch.getAABBInstancedDataVboID(), instancedVBOData, GL41.GL_DYNAMIC_DRAW);
            GL41.glBufferSubData(GL41.GL_ARRAY_BUFFER, 0, instancedVBOData);
        });
    }

    private float[] getAABBInstancedVBOData(MeshMaterialBatch batch) {
        float[] vboData = new float[batch.getNumOfEntities() * INSTANCED_DATA_AABB_SIZE_IN_BYTES];
        int offset = 0;

        int index = 0;
        for (int entityId : batch.getEntities()) {
            RenderComponent component = ECS.world.getEntity(entityId).getComponent(RenderComponent.class);
            component.transform.getTransformationMatrixWithoutRotation().get(vboData, offset);
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

    private void loadAABBBatchMeshDataIntoVAO(MeshMaterialBatch batch) {
        LOGGER.trace("    Loading AABB data {} into [VAO:{}]", batch.getMesh(), batch.getAABBVaoID());

        // AABB vertices
        int aabbVboID = createAndStoreDataInVBO(batch.getMesh().getAABBVertices());
        vbos.add(aabbVboID);
        LOGGER.trace("    AABB-VBO:{}", aabbVboID);
        addVAOAttribute(aabbVboID, LayoutQualifier.VX_POSITION.location, 3);
        batch.setAABBVBOID(aabbVboID);

        // AABB indices
        int aabbIndicesVboID = createAndStoreDataInVBO(batch.getMesh().getAABBIndices());
        vbos.add(aabbIndicesVboID);
        LOGGER.trace("    AABB_INDICES-VBO:{}", aabbIndicesVboID);
        batch.setAABBIndicesVBOID(aabbIndicesVboID);
    }
}
