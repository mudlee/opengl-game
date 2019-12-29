package spck.engine.framework;

import org.joml.AABBf;
import org.lwjgl.opengl.GL41;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.debug.Stats;
import spck.engine.ecs.ECS;
import spck.engine.render.GPUDataStore;
import spck.engine.render.MeshMaterialBatch;
import spck.engine.render.RenderComponent;

public class OpenGLAABBGPUDataStore extends OpenGLAbstractGPUDataStore implements GPUDataStore<MeshMaterialBatch> {
    public enum LayoutQualifier {
        VX_POSITION(0);

        public int location;

        LayoutQualifier(int location) {
            this.location = location;
        }
    }

    public static final int NUMBER_OF_INDICES_PER_AABB = 36;
    private static final int NUMBER_OF_VERTICES_PER_AABB = 24;
    private static final AABBf REUSABLE_AABB = new AABBf();
    private static final Logger log = LoggerFactory.getLogger(OpenGLAABBGPUDataStore.class);
    private static final int[] BASE_INDICES = new int[]{
            0, 1, 2, 1, 3, 2, // front
            3, 1, 5, 3, 5, 4, // right
            4, 5, 7, 7, 6, 4, // back
            6, 7, 0, 0, 2, 6, // left
            0, 7, 5, 5, 1, 0, // bottom
            2, 3, 4, 4, 6, 2, // top
    };

    public OpenGLAABBGPUDataStore() {
        super(-1);
    }

    @Override
    public void uploadDataToGPU(MeshMaterialBatch batch) {
        GL.genVaoContext(vaoId -> {
            log.trace("AABB VAO created {}", vaoId);
            vaos.add(vaoId);
            batch.setAABBVaoID(vaoId);

            loadAABBDataIntoVao(batch);
        }, () -> GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, 0));
        batch.dataUpdated();
    }

    @Override
    public void updateDataInGPU(MeshMaterialBatch batch) {
        if (batch.getNumOfEntities() == 0) {
            log.trace("Batch {} is empty, removing its data from GPU", batch.getID());
            Stats.vboMemoryUsed -= batch.getOldSize() * batch.getEntityMemoryUsage();
            removeBatchDataFromGPU(batch);
            return;
        }

        GL.bufferContext(batch.getAABBVerticesVboID(), () -> {
            log.trace("Updating batch {} AABB data in GPU. Size: {}->{}",
                    batch.getID(),
                    batch.getOldSize(),
                    batch.getNumOfEntities()
            );
            float[] data = getVertices(batch);
            if (batch.wasSizeChanged()) {
                // updating the storage size
                GL41.glBufferData(GL41.GL_ARRAY_BUFFER, batch.getNumOfEntities() * NUMBER_OF_VERTICES_PER_AABB * Float.BYTES, GL41.GL_DYNAMIC_DRAW);
            }
            GL41.glBufferSubData(GL41.GL_ARRAY_BUFFER, 0, data);
            log.trace("    AABB Data has been updated for batch {}", batch.getID());
        });
    }

    private void removeBatchDataFromGPU(MeshMaterialBatch batch) {
        log.trace("Removing Batch {} AABB data from GPU...", batch.getID());
        // Batch is now empty, delete datas
        GL41.glDeleteVertexArrays(batch.getAABBVaoID());
        GL41.glDeleteBuffers(batch.getAABBVerticesVboID());
        GL41.glDeleteBuffers(batch.getAABBIndicesVboID());
        vbos.remove(batch.getAABBVerticesVboID());
        vbos.remove(batch.getAABBIndicesVboID());
        vaos.remove(batch.getAABBVaoID());
        log.trace("Batch {} AABB data removed from GPU", batch.getID());
    }

    private void loadAABBDataIntoVao(MeshMaterialBatch batch) {
        float[] vertices = getVertices(batch);

        // AABB vertices
        int aabbVboID = createAndStoreDataInVBO(vertices);
        vbos.add(aabbVboID);
        log.trace("    AABB-VBO:{}", aabbVboID);
        addVAOAttribute(aabbVboID, LayoutQualifier.VX_POSITION.location, 3);
        batch.setAABBVBOID(aabbVboID);

        // AABB indices
        int aabbIndicesVboID = createAndStoreDataInVBO(calculateIndices(batch.getNumOfEntities()));
        vbos.add(aabbIndicesVboID);
        log.trace("    AABB_INDICES-VBO:{}", aabbIndicesVboID);
        batch.setAABBIndicesVBOID(aabbIndicesVboID);
    }

    private float[] getVertices(MeshMaterialBatch batch) {
        float[] vertices = new float[batch.getNumOfEntities() * NUMBER_OF_VERTICES_PER_AABB];

        int index = 0;
        for (Integer entityId : batch.getEntities()) {
            RenderComponent component = ECS.getWorld().getEntity(entityId).getComponent(RenderComponent.class);
            batch.getMesh().getAABB().transform(component.transform.getTransformationMatrix(), REUSABLE_AABB);
            for (float vertex : calculateVertices(REUSABLE_AABB)) {
                vertices[index++] = vertex;
            }
        }

        return vertices;
    }

    private int[] calculateIndices(int numberOfEntities) {
        int[] indices = new int[numberOfEntities * NUMBER_OF_INDICES_PER_AABB];
        int index = 0;
        for (int i = 0; i < numberOfEntities; i++) {
            for (int j = 0; j < NUMBER_OF_INDICES_PER_AABB; j++) {
                indices[index++] = BASE_INDICES[j] + i * 8;
            }
        }

        return indices;
    }

    private float[] calculateVertices(AABBf aabb) {
        return new float[]{
                aabb.minX, aabb.minY, aabb.maxZ, // front bottom left & left bottom right- 0
                aabb.maxX, aabb.minY, aabb.maxZ, // front bottom right & right bottom left & bottom top right - 1
                aabb.minX, aabb.maxY, aabb.maxZ, // front top left & left top right & top bottom left- 2
                aabb.maxX, aabb.maxY, aabb.maxZ, // front top right & right top left & top bottom right - 3

                aabb.maxX, aabb.maxY, aabb.minZ, // right top right & back top left & top top right - 4
                aabb.maxX, aabb.minY, aabb.minZ, // right bottom right & back bottom left & bottom bottom right - 5
                aabb.minX, aabb.maxY, aabb.minZ, // back top right & left top left & top top left - 6
                aabb.minX, aabb.minY, aabb.minZ, // back bottom right & left bottom left & bottom bottom left- 7
        };
    }
}
