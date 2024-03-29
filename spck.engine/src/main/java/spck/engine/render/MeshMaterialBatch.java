package spck.engine.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class MeshMaterialBatch {
    private final static Logger LOGGER = LoggerFactory.getLogger(MeshMaterialBatch.class);
    private final String ID;
    private final Mesh mesh;
    private final Material material;
    private final Set<Integer> entities = new HashSet<>();
    private int vaoID;
    private Integer instancedVboID;
    private Integer verticesVBOID;
    private Integer indicesVBOID;
    private Integer normalsVBOID;
    private Integer uvVBOId;
    private boolean batchSizeChanged = false;
    private int currentSize;
    private int oldSize;
    private int entityMemoryUsage = 0;
    private int aabbVaoID;
    private int aabbVerticesVboID;
    private int aabbIndicesVboID;

    public MeshMaterialBatch(Mesh mesh, Material material) {
        this.mesh = mesh;
        this.material = material;
        this.ID = material.hashCode() + "-" + mesh.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeshMaterialBatch)) return false;

        MeshMaterialBatch batch = (MeshMaterialBatch) o;

        return ID.equals(batch.ID);
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    public String getID() {
        return ID;
    }

    public void addEntity(int entityId) {
        if (entities.contains(entityId)) {
            throw new RuntimeException(String.format("Entity %s is already added", entityId));
        }

        entities.add(entityId);
        batchSizeChanged = true;
        int oldSize = currentSize;
        currentSize++;
        LOGGER.trace("Entity {} added to batch {}, size {}->{}", entityId, ID, oldSize, currentSize);
    }

    public void removeEntity(Integer entityId) {
        entities.remove(entityId);
        batchSizeChanged = true;
        int oldSize = currentSize;
        currentSize--;
        LOGGER.debug("Entity {} removed from batch {}, size {}->{}", entityId, ID, oldSize, currentSize);
    }

    public void dataUpdated() {
        oldSize = currentSize;
        batchSizeChanged = false;
    }

    public int getOldSize() {
        return oldSize;
    }

    public void setVaoID(int vaoID) {
        this.vaoID = vaoID;
    }

    public void setInstancedVboID(Integer instancedVboID) {
        this.instancedVboID = instancedVboID;
    }

    public void setVerticesVBOId(Integer verticesVBOId) {
        this.verticesVBOID = verticesVBOId;
    }

    public void setIndicesVBOID(Integer indicesVBOID) {
        this.indicesVBOID = indicesVBOID;
    }

    public void setNormalsVBOId(Integer normalsVBOId) {
        this.normalsVBOID = normalsVBOId;
    }

    public void setUVVBOId(Integer textureVBOId) {
        this.uvVBOId = textureVBOId;
    }

    public void setAABBVaoID(int vaoId) {
        this.aabbVaoID = vaoId;
    }

    public void setAABBVBOID(int aabbVboID) {
        this.aabbVerticesVboID = aabbVboID;
    }

    public void setAABBIndicesVBOID(int aabbIndicesVboID) {
        this.aabbIndicesVboID = aabbIndicesVboID;
    }

    public void storeEntityMemoryUsage(int bytes) {
        entityMemoryUsage = bytes;
    }

    public boolean wasSizeChanged() {
        return batchSizeChanged;
    }

    public int getNumOfEntities() {
        return entities.size();
    }

    public Set<Integer> getEntityIDs() {
        return entities;
    }

    public Set<Integer> getEntities() {
        return entities;
    }

    public Integer getVaoID() {
        return vaoID;
    }

    public Integer getInstancedVboID() {
        return instancedVboID;
    }

    public Integer getNormalsVBOID() {
        return normalsVBOID;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Integer getVerticesVBOID() {
        return verticesVBOID;
    }

    public Integer getIndicesVBOID() {
        return indicesVBOID;
    }

    public Integer getUvVBOId() {
        return uvVBOId;
    }

    public Material getMaterial() {
        return material;
    }

    public int getEntityMemoryUsage() {
        return entityMemoryUsage;
    }

    public int getAABBVaoID() {
        return aabbVaoID;
    }

    public int getAABBVerticesVboID() {
        return aabbVerticesVboID;
    }

    public int getAABBIndicesVboID() {
        return aabbIndicesVboID;
    }
}
