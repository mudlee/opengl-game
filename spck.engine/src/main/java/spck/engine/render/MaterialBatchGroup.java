package spck.engine.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MaterialBatchGroup {
    private final static Logger LOGGER = LoggerFactory.getLogger(MaterialBatchGroup.class);
    private final Material material;
    private final Map<Integer, MeshMaterialBatch> batches = new HashMap<>();

    public MaterialBatchGroup(Material material) {
        this.material = material;
    }

    public boolean containsBatch(int batchID) {
        return batches.containsKey(batchID);
    }

    public void addBatch(int meshID, MeshMaterialBatch batch) {
        if (containsBatch(meshID)) {
            throw new RuntimeException(String.format("Mesh %s is already in the group", meshID));
        }

        batches.put(meshID, batch);
        LOGGER.trace("Mesh {} added", meshID);
    }

    public Optional<MeshMaterialBatch> getBatch(int batchID) {
        return Optional.ofNullable(batches.getOrDefault(batchID, null));
    }

    public Material getMaterial() {
        return material;
    }

    public Map<Integer, MeshMaterialBatch> getBatches() {
        return batches;
    }
}
