package spck.engine.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class Batch {
    private final static Logger LOGGER = LoggerFactory.getLogger(Batch.class);
    private final String ID;
    private final Mesh mesh;
    private final Material material;
    private final Set<Integer> entities = new HashSet<>();
    private int vaoID;
    private Integer instancedVboID;
    private Integer verticesVBOId;
    private Integer indicesVBOId;
    private Integer normalsVBOId;
    private Integer uvVBOId;
    private boolean batchSizeChanged = false;
    private int currentSize;
    private int oldSize;
    private int entityMemoryUsage = 0;

    public Batch(Mesh mesh, Material material) {
        this.mesh = mesh;
        this.material = material;
        this.ID = material.hashCode() + "-" + mesh.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Batch)) return false;

        Batch batch = (Batch) o;

        return ID.equals(batch.ID);
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    public String getID() {
        return ID;
    }

    public void add(int entityId) {
        if (entities.contains(entityId)) {
            throw new RuntimeException(String.format("Entity %s is already added", entityId));
        }

        entities.add(entityId);
        batchSizeChanged = true;
        int oldSize = currentSize;
        currentSize++;
        LOGGER.trace("Entity {} added to batch {}, size {}->{}", entityId, ID, oldSize, currentSize);
    }

    public void remove(Integer entityId) {
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
        this.verticesVBOId = verticesVBOId;
    }

    public void setIndicesVBOId(Integer indicesVBOId) {
        this.indicesVBOId = indicesVBOId;
    }

    public void setNormalsVBOId(Integer normalsVBOId) {
        this.normalsVBOId = normalsVBOId;
    }

    public void setUVVBOId(Integer textureVBOId) {
        this.uvVBOId = textureVBOId;
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

    public Set<Integer> getEntities() {
        return entities;
    }

    public Integer getVaoID() {
        return vaoID;
    }

    public Integer getInstancedVboID() {
        return instancedVboID;
    }

    public Integer getNormalsVBOId() {
        return normalsVBOId;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Integer getVerticesVBOId() {
        return verticesVBOId;
    }

    public Integer getIndicesVBOId() {
        return indicesVBOId;
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
}
