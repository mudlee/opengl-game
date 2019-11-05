package spck.engine.ecs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.render.*;

import java.util.*;
import java.util.function.Supplier;

public class EntityBatchStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityBatchStore.class);
    private final Map<Integer, Integer> entityBatchGroupIDMap = new HashMap<>();
    private final Map<Integer, Integer> entityBatchIDMap = new HashMap<>();
    private final Map<Integer, MaterialBatchGroup> groups = new HashMap<>();
    private final Set<Integer> entities = new HashSet<>();
    /**
     * Batches in this list are new,
     * so all data have to be uploaded to the GPU before render
     */
    private final Set<MeshMaterialBatch> newBatchDataQueue = new HashSet<>();
    /**
     * Batches' data in this list are already uploaded to the GPU,
     * but these batches are changed, so data have to be updated
     */
    private final Set<MeshMaterialBatch> changedBatchDataQueue = new HashSet<>();

    private final Map<Integer, MeshMaterialPair> materialChanges = new HashMap<>();
    private final Map<Integer, MeshMaterialPair> meshChanges = new HashMap<>();
    private final Map<Integer, RenderComponent> transformationChanges = new HashMap<>();

    public void add(int entityId, MeshMaterialPair meshMaterialPair) {
        boolean addingEntityAsNew = !entities.contains(entityId);
        entities.add(entityId);

        Mesh mesh = meshMaterialPair.getMesh();
        Material material = meshMaterialPair.getMaterial();
        int materialID = material.hashCode();
        int meshID = mesh.hashCode();


        entityBatchGroupIDMap.put(entityId, materialID);
        entityBatchIDMap.put(entityId, meshID);
        LOGGER.trace("{} entity {} to BatchGroup {} Batch {}", addingEntityAsNew ? "Adding" : "Updating", entityId, materialID, meshID);

        if (!groups.containsKey(materialID)) {
            LOGGER.trace("    BatchGroup {} does not exist, creating...", materialID);
            // we clone the material for the batch group, because we can change entity's material and it should not affect
            // the batch group's material
            // Note: performance? -> use a material pool inside the batching system
            groups.put(materialID, new MaterialBatchGroup(material));
        }

        if (!groups.get(materialID).containsBatch(meshID)) {
            LOGGER.trace("    Batch {} does not exist, creating...", meshID);
            MeshMaterialBatch batch = new MeshMaterialBatch(mesh, material);
            groups.get(materialID).addBatch(meshID, batch);
            newBatchDataQueue.add(batch);
            LOGGER.trace("    Batch {} is added to the new queue", meshID);
        }

        MeshMaterialBatch targetBatch = groups.get(materialID).getBatch(meshID).orElseThrow(() -> new RuntimeException(String.format("Batch %d was not found in BatchGroup %d", meshID, materialID)));

        if (!newBatchDataQueue.contains(targetBatch)) {
            // If multiple entities are added before we are processing the data changes for the GPU,
            // newBatchGroupDataQueue might contain a Batch that has already been waiting for the data upload,
            // so we don't have to change one of its batch's data as well
            changedBatchDataQueue.add(targetBatch);
            LOGGER.trace("    Batch {} is added to the changed queue", meshID);
        }

        targetBatch.addEntity(entityId);
        LOGGER.trace("    Entity {} added to BatchGroup {} Batch {}", entityId, materialID, meshID);
    }

    public boolean containsEntity(int entityId) {
        return entities.contains(entityId);
    }

    public void entityMaterialHasChanged(int entityId, MeshMaterialPair meshMaterialPair) {
        if (!entities.contains(entityId)) {
            throw new RuntimeException(String.format("Entity %d material changed, but it's not in the batch store", entityId));
        }

        LOGGER.trace("Entity {} material changed, new: {}", entityId, meshMaterialPair.getMaterial().hashCode());
        materialChanges.put(entityId, meshMaterialPair);
    }

    public void entityTransformHasChanged(int entityId, RenderComponent component) {
        if (!entities.contains(entityId)) {
            throw new RuntimeException(String.format("Entity %d transform changed, but it's not in the batch store", entityId));
        }

        LOGGER.trace("Entity {} transform changed, new: {}", entityId, component.transform.hashCode());
        transformationChanges.put(entityId, component);
    }

    public void entityMeshHasChanged(int entityId, MeshMaterialPair meshMaterialPair) {
        if (!entities.contains(entityId)) {
            throw new RuntimeException(String.format("Entity %d mesh changed, but it's not in the batch store", entityId));
        }

        LOGGER.trace("Entity {} mesh changed, new: {}", entityId, meshMaterialPair.getMesh().hashCode());
        meshChanges.put(entityId, meshMaterialPair);
    }

    public void processChanges() {
        processMaterialChanges();
        processMeshChanges();
        processTransformationChanges();
        removeEmptyBatches();
        processGPUDataChanges();
    }

    public void destroyEntity(int id) {
        LOGGER.trace("Removing entity {}", id);
        ECS.world.delete(id);
        entities.remove(id);

        int batchGroupID = getEntityBatchGroupID(id).orElseThrow(() -> new RuntimeException(String.format("Entity's %s was marked for deletion, but it's not in the batching system", id)));
        int batchID = getEntityBatchID(id).orElseThrow(() -> new RuntimeException(String.format("Entity's %s was marked for deletion, but it's not in the batching system", id)));

        groups.get(batchGroupID)
                .getBatch(batchID)
                .orElseThrow(() -> new RuntimeException(String.format("Batch %d was not found in BatchGroup %d", batchID, batchGroupID)))
                .removeEntity(id);

        entityBatchGroupIDMap.remove(id);
        entityBatchIDMap.remove(id);
        materialChanges.remove(id);
        meshChanges.remove(id);
        transformationChanges.remove(id);
    }

    public Map<Integer, MaterialBatchGroup> getGroups() {
        return groups;
    }

    private void processGPUDataChanges() {
        if (!newBatchDataQueue.isEmpty()) {
            LOGGER.trace("Processing new Batches' data...");
            newBatchDataQueue.forEach(batch -> batch.getMaterial().getGpuDataStore().uploadBatchDataToGPU(batch));
            newBatchDataQueue.clear();
            LOGGER.trace("New Batches' data processed");
        }

        if (!changedBatchDataQueue.isEmpty()) {
            LOGGER.trace("Processing changed Batches' data...");
            changedBatchDataQueue.forEach(batch -> batch.getMaterial().getGpuDataStore().updateBatchDataInGPU(batch));
            changedBatchDataQueue.clear();
            LOGGER.trace("Changed Batches' data processed");
        }
    }

    private void removeEmptyBatches() {
        Iterator<Map.Entry<Integer, MaterialBatchGroup>> groupIterator = groups.entrySet().iterator();
        while (groupIterator.hasNext()) {
            Map.Entry<Integer, MaterialBatchGroup> groupEntry = groupIterator.next();

            Iterator<Map.Entry<Integer, MeshMaterialBatch>> batchIterator = groupEntry.getValue().getBatches().entrySet().iterator();
            while (batchIterator.hasNext()) {
                Map.Entry<Integer, MeshMaterialBatch> batchEntry = batchIterator.next();
                if (batchEntry.getValue().getNumOfEntities() == 0) {
                    batchEntry.getValue().getMaterial().getGpuDataStore().updateBatchDataInGPU(batchEntry.getValue());
                    LOGGER.trace("Batch {} is empty, removing...", batchEntry.getValue().getID());
                    LOGGER.trace("Batch {} removed from newBatchDataQueue: {}", batchEntry.getValue().getID(), newBatchDataQueue.remove(batchEntry.getValue()));
                    LOGGER.trace("Batch {} removed from changedBatchDataQueue: {}", batchEntry.getValue().getID(), changedBatchDataQueue.remove(batchEntry.getValue()));
                    batchIterator.remove();
                }
            }

            if (groupEntry.getValue().getBatches().isEmpty()) {
                LOGGER.trace("BatchGroup {} is empty, removing...", groupEntry.getValue().hashCode());
                groupIterator.remove();
            }
        }
    }

    private void processTransformationChanges() {
        if (transformationChanges.isEmpty()) {
            return;
        }

        LOGGER.trace("Processing transformation changes...");
        transformationChanges.forEach((entityId, component) -> {
            int batchGroupID = getEntityBatchGroupID(entityId).orElseThrow(() -> new RuntimeException(String.format("Entity's %s transform was changed, but it's not in the batching system", entityId)));
            int batchID = getEntityBatchID(entityId).orElseThrow(() -> new RuntimeException(String.format("Entity's %s transform was changed, but it's not in the batching system", entityId)));

            MeshMaterialBatch batch = groups.get(batchGroupID)
                    .getBatch(batchID)
                    .orElseThrow(() -> new RuntimeException(String.format("Batch %d was not found in BatchGroup %d", batchID, batchGroupID)));

            if (newBatchDataQueue.contains(batch)) {
                LOGGER.trace(
                        "BatchGroup {} Batch {} data won't be updated as it's still waiting for data uploading",
                        batchGroupID,
                        batchID
                );
            } else {
                changedBatchDataQueue.add(batch);
            }
        });
        transformationChanges.clear();
        LOGGER.trace("Transformation changes are applied");
    }

    private void processMeshChanges() {
        if (meshChanges.isEmpty()) {
            return;
        }

        LOGGER.trace("Processing mesh changes...");
        meshChanges.forEach((entityId, meshMaterialPair) -> {
            LOGGER.trace("{}'s mesh has changed", entityId);

            Supplier<RuntimeException> exceptionSupplier = () -> new RuntimeException(String.format("Entity %s mesh was changed, but it's not in the batching system", entityId));
            int batchGroupID = getEntityBatchGroupID(entityId).orElseThrow(exceptionSupplier);
            int batchID = getEntityBatchID(entityId).orElseThrow(exceptionSupplier);

            groups.get(batchGroupID)
                    .getBatch(batchID)
                    .orElseThrow(() -> new RuntimeException(String.format("Batch %d was not found in BatchGroup %d", batchID, batchGroupID)))
                    .removeEntity(entityId);

            add(entityId, meshMaterialPair);
        });

        LOGGER.trace("Mesh changes are applied");
        meshChanges.clear();
    }

    private void processMaterialChanges() {
        if (materialChanges.isEmpty()) {
            return;
        }

        LOGGER.trace("Processing material changes...");
        materialChanges.forEach((entityId, meshMaterialPair) -> {
            Supplier<RuntimeException> exceptionSupplier = () -> new RuntimeException(String.format("Entity's %s material was changed, but it's not in the batching system", entityId));

            int batchGroupID = getEntityBatchGroupID(entityId).orElseThrow(exceptionSupplier);
            int batchID = getEntityBatchID(entityId).orElseThrow(exceptionSupplier);
            LOGGER.trace("{}'s material has changed, removing from BatchGroup {} Batch {}, and adding to BatchGroup {} Batch {}", entityId, batchGroupID, batchID, meshMaterialPair.getMaterial().hashCode(), meshMaterialPair.getMesh().hashCode());

            groups.get(batchGroupID)
                    .getBatch(batchID)
                    .orElseThrow(() -> new RuntimeException(String.format("Batch %d was not found in BatchGroup %d", batchID, batchGroupID)))
                    .removeEntity(entityId);

            add(entityId, meshMaterialPair);
        });

        materialChanges.clear();
        LOGGER.trace("Material changes are applied");
    }

    private Optional<Integer> getEntityBatchGroupID(int entityId) {
        return Optional.ofNullable(entityBatchGroupIDMap.getOrDefault(entityId, null));
    }

    private Optional<Integer> getEntityBatchID(int entityId) {
        return Optional.ofNullable(entityBatchIDMap.getOrDefault(entityId, null));
    }
}
