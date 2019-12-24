package spck.engine.framework;

import org.lwjgl.opengl.GL41;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.debug.Stats;
import spck.engine.ecs.ECS;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.render.GPUDataStore;
import spck.engine.render.Material;
import spck.engine.render.MeshMaterialBatch;
import spck.engine.render.textures.TextureUVModifier;

import java.util.function.Supplier;

public class OpenGLDefaultGPUMeshDataStore extends OpenGLAbstractGPUDataStore implements GPUDataStore<MeshMaterialBatch> {
    public enum LayoutQualifier {
        VX_POSITION(0),
        VX_NORMAL(1),
        VX_UV_COORDS(2),
        INS_TRANSFORMATION_MATRIX_COL1(3),
        INS_TRANSFORMATION_MATRIX_COL2(4),
        INS_TRANSFORMATION_MATRIX_COL3(5),
        INS_TRANSFORMATION_MATRIX_COL4(6),
        INS_UV_SCALE(7),
        INS_UV_OFFSET(8);

        public int location;

        LayoutQualifier(int location) {
            this.location = location;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(OpenGLDefaultGPUMeshDataStore.class);
    // transformationMatrixInstanced(4x4) + uv scale(1) + uv offset(2)
    private static final int INSTANCED_DATA_SIZE_IN_BYTES = 19;

    public OpenGLDefaultGPUMeshDataStore() {
        super(INSTANCED_DATA_SIZE_IN_BYTES);
    }

    @Override
    public void uploadDataToGPU(MeshMaterialBatch batch) {
        log.trace("Uploading data to GPU for batch {}, num of Entities: {}", batch.getID(), batch.getNumOfEntities());
        GL.genVaoContext(vaoId -> {
            log.trace("VAO created {}", vaoId);
            vaos.add(vaoId);
            batch.setVaoID(vaoId);

            loadBatchMeshDataIntoVAO(batch);
            setupInstancedRendering(batch);
            loadInstancedRenderingData(batch);
        }, () -> GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, 0));
    }

    @Override
    public void updateDataInGPU(MeshMaterialBatch batch) {
        if (batch.getNumOfEntities() == 0) {
            log.trace("Batch {} is empty, removing its data from GPU", batch.getID());
            Stats.vboMemoryUsed -= batch.getOldSize() * batch.getEntityMemoryUsage();
            removeBatchDataFromGPU(batch);
            return;
        }

        GL.bufferContext(batch.getInstancedVboID(), () -> {
            log.trace("Updating batch {} data in GPU. Size: {}->{}",
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
            log.trace("    Data has been updated for batch {}", batch.getID());
        });
    }

    private void removeBatchDataFromGPU(MeshMaterialBatch batch) {
        log.trace("Removing Batch {} data from GPU...", batch.getID());
        // Batch is now empty, delete datas
        GL41.glDeleteVertexArrays(batch.getVaoID());
        GL41.glDeleteBuffers(batch.getIndicesVBOID());
        GL41.glDeleteBuffers(batch.getNormalsVBOID());
        GL41.glDeleteBuffers(batch.getVerticesVBOID());
        GL41.glDeleteBuffers(batch.getInstancedVboID());
        vbos.remove(batch.getIndicesVBOID());
        vbos.remove(batch.getNormalsVBOID());
        vbos.remove(batch.getVerticesVBOID());
        vbos.remove(batch.getInstancedVboID());

        if (batch.getUvVBOId() != null) {
            GL41.glDeleteBuffers(batch.getUvVBOId());
            vbos.remove(batch.getUvVBOId());
        }

        vaos.remove(batch.getVaoID());
        log.trace("Batch {} data removed from GPU", batch.getID());
    }

    private void setupInstancedRendering(MeshMaterialBatch batch) {
        log.trace("    Setting up instanced rendering for {} in [VAO:{}]", batch.getMesh(), batch.getVaoID());

        // Create VBO for instanced attributes
        int instancedDataVboId = GL41.glGenBuffers();
        vbos.add(instancedDataVboId);
        log.trace("    INSTANCED_DATA-VBO:{}", instancedDataVboId);
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

    private void loadInstancedRenderingData(MeshMaterialBatch batch) {
        GL.bufferContext(batch.getInstancedVboID(), () -> {
            float[] instancedVBOData = getInstancedVBOData(batch);
            GL41.glBufferData(batch.getInstancedVboID(), instancedVBOData, GL41.GL_DYNAMIC_DRAW);
            GL41.glBufferSubData(GL41.GL_ARRAY_BUFFER, 0, instancedVBOData);
        });
    }

    private float[] getInstancedVBOData(MeshMaterialBatch batch) {
        float[] vboData = new float[batch.getNumOfEntities() * INSTANCED_DATA_SIZE_IN_BYTES];
        int offset = 0;

        int index = 0;
        for (int entityId : batch.getEntities()) {
            RenderComponent component = ECS.getWorld().getEntity(entityId).getComponent(RenderComponent.class);
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

    private void loadBatchMeshDataIntoVAO(MeshMaterialBatch batch) {
        log.trace("    Loading mesh {} into [VAO:{}]", batch.getMesh(), batch.getVaoID());

        // vertices
        int verticesVboID = createAndStoreDataInVBO(batch.getMesh().getVertices());
        vbos.add(verticesVboID);
        log.trace("    VERTICES-VBO:{}", verticesVboID);
        batch.setVerticesVBOId(verticesVboID);
        addVAOAttribute(verticesVboID, LayoutQualifier.VX_POSITION.location, 3);

        // indices
        int indicesVboID = createAndStoreDataInVBO(batch.getMesh().getIndices());
        vbos.add(indicesVboID);
        log.trace("    INDICES-VBO:{}", indicesVboID);
        batch.setIndicesVBOID(indicesVboID);

        // normals
        int normalsVboID = createAndStoreDataInVBO(batch.getMesh().getNormals());
        vbos.add(normalsVboID);
        log.trace("    NORMALS-VBO:{}", normalsVboID);
        addVAOAttribute(normalsVboID, LayoutQualifier.VX_NORMAL.location, 3);
        batch.setNormalsVBOId(normalsVboID);

        // UV coords
        if (batch.getMaterial().hasDiffuseTexture()) {
            if (batch.getMesh().getUVCoords().length == 0) {
                throw new RuntimeException(String.format("Batch %s has diffuse texture, but no UV coords set for the mesh", batch.getID()));
            }

            int uvVboId = createAndStoreDataInVBO(batch.getMesh().getUVCoords());
            log.trace("    UV_COORDS-VBO:{}", uvVboId);
            addVAOAttribute(uvVboId, LayoutQualifier.VX_UV_COORDS.location, 2);
            batch.setUVVBOId(uvVboId);
        }
    }
}