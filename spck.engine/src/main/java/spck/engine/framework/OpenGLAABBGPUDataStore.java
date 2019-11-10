package spck.engine.framework;

import org.joml.AABBf;
import org.joml.Matrix4fc;
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
        VX_POSITION(0);

        public int location;

        LayoutQualifier(int location) {
            this.location = location;
        }
    }

    public static final int NUMBER_OF_INDICES_PER_AABB = 36;
    private static final int NUMBER_OF_VERTICES_PER_AABB = 24;

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGLAABBGPUDataStore.class);
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
    public void uploadBatchDataToGPU(MeshMaterialBatch batch) {
        GL.genVaoContext(vaoId -> {
            LOGGER.trace("AABB VAO created {}", vaoId);
            vaos.add(vaoId);
            batch.setAABBVaoID(vaoId);

            loadAABBDataIntoVao(batch);
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

        GL.bufferContext(batch.getAABBVerticesVboID(), () -> {
            LOGGER.trace("Updating batch {} AABB data in GPU. Size: {}->{}",
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
            LOGGER.trace("    AABB Data has been updated for batch {}", batch.getID());
        });
    }

    private void removeBatchDataFromGPU(MeshMaterialBatch batch) {
        LOGGER.trace("Removing Batch {} AABB data from GPU...", batch.getID());
        // Batch is now empty, delete datas
        GL41.glDeleteVertexArrays(batch.getAABBVaoID());
        GL41.glDeleteBuffers(batch.getAABBVerticesVboID());
        GL41.glDeleteBuffers(batch.getAABBIndicesVboID());
        vbos.remove(batch.getAABBVerticesVboID());
        vbos.remove(batch.getAABBIndicesVboID());
        vaos.remove(batch.getAABBVaoID());
        LOGGER.trace("Batch {} AABB data removed from GPU", batch.getID());
    }

    private void loadAABBDataIntoVao(MeshMaterialBatch batch) {
        float[] vertices = getVertices(batch);

        int index = 0;
        for (Integer entityId : batch.getEntities()) {
            RenderComponent component = ECS.world.getEntity(entityId).getComponent(RenderComponent.class);
            AABBf transformedAABB = transform(batch.getMesh().getAABB(), component.transform.getTransformationMatrix());
            for (float vertex : calculateVertices(transformedAABB)) {
                vertices[index++] = vertex;
            }
        }

        // AABB vertices
        int aabbVboID = createAndStoreDataInVBO(vertices);
        vbos.add(aabbVboID);
        LOGGER.trace("    AABB-VBO:{}", aabbVboID);
        addVAOAttribute(aabbVboID, LayoutQualifier.VX_POSITION.location, 3);
        batch.setAABBVBOID(aabbVboID);

        // AABB indices
        int aabbIndicesVboID = createAndStoreDataInVBO(calculateIndices(batch.getNumOfEntities()));
        vbos.add(aabbIndicesVboID);
        LOGGER.trace("    AABB_INDICES-VBO:{}", aabbIndicesVboID);
        batch.setAABBIndicesVBOID(aabbIndicesVboID);
    }

    private float[] getVertices(MeshMaterialBatch batch) {
        float[] vertices = new float[batch.getNumOfEntities() * NUMBER_OF_VERTICES_PER_AABB];

        int index = 0;
        for (Integer entityId : batch.getEntities()) {
            RenderComponent component = ECS.world.getEntity(entityId).getComponent(RenderComponent.class);
            AABBf transformedAABB = transform(batch.getMesh().getAABB(), component.transform.getTransformationMatrix());
            for (float vertex : calculateVertices(transformedAABB)) {
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

    // TODO: remove this, when JOML 1.9.20 is out
    public AABBf transform(AABBf source, Matrix4fc m) {
        AABBf dest = new AABBf();
        float dx = source.maxX - source.minX, dy = source.maxY - source.minY, dz = source.maxZ - source.minZ;
        float minx = Float.POSITIVE_INFINITY, miny = Float.POSITIVE_INFINITY, minz = Float.POSITIVE_INFINITY;
        float maxx = Float.NEGATIVE_INFINITY, maxy = Float.NEGATIVE_INFINITY, maxz = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 8; i++) {
            float x = source.minX + (i & 1) * dx, y = source.minY + (i >> 1 & 1) * dy, z = source.minZ + (i >> 2 & 1) * dz;
            float tx = m.m00() * x + m.m10() * y + m.m20() * z + m.m30();
            float ty = m.m01() * x + m.m11() * y + m.m21() * z + m.m31();
            float tz = m.m02() * x + m.m12() * y + m.m22() * z + m.m32();
            minx = Math.min(tx, minx);
            miny = Math.min(ty, miny);
            minz = Math.min(tz, minz);
            maxx = Math.max(tx, maxx);
            maxy = Math.max(ty, maxy);
            maxz = Math.max(tz, maxz);
        }
        dest.minX = minx;
        dest.minY = miny;
        dest.minZ = minz;
        dest.maxX = maxx;
        dest.maxY = maxy;
        dest.maxZ = maxz;
        return dest;
    }
}
