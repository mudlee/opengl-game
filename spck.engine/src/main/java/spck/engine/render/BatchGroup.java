package spck.engine.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BatchGroup {
    private final static Logger LOGGER = LoggerFactory.getLogger(BatchGroup.class);
    private final Material material;
    private final Map<Integer, Batch> batches = new HashMap<>();

    public BatchGroup(Material material) {
        this.material = material;
    }

    public boolean containsBatch(int batchID) {
        return batches.containsKey(batchID);
    }

    public void addBatch(int batchID, Batch batch) {
        if (containsBatch(batchID)) {
            throw new RuntimeException("Batch {} is alread in the group");
        }

        batches.put(batchID, batch);
        LOGGER.debug("Batch {} added", batchID);
    }

    public Optional<Batch> getBatch(int batchID) {
        return Optional.ofNullable(batches.getOrDefault(batchID, null));
    }

    public Material getMaterial() {
        return material;
    }

    public Map<Integer, Batch> getBatches() {
        return batches;
    }
}
