package spck.engine.ecs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.Engine;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.render.*;

import java.util.*;
import java.util.function.Supplier;

public class EntityBatchStore {
    private static class BatchGroupBatchTouple {
        int batchGroupID;
        int batchID;

        BatchGroupBatchTouple(int batchGroupID, int batchID) {
            this.batchGroupID = batchGroupID;
            this.batchID = batchID;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityBatchStore.class);
    private final Map<Integer, List<BatchGroupBatchTouple>> entityBatchGroupBatchIDMap = new HashMap<>();
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


        entityBatchGroupBatchIDMap.putIfAbsent(entityId, new ArrayList<>());
        entityBatchGroupBatchIDMap.get(entityId).add(new BatchGroupBatchTouple(materialID, meshID));

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

    public void destroyEntity(int entityId) {
        LOGGER.trace("Removing entity {}", entityId);
        ECS.world.delete(entityId);
        entities.remove(entityId);

        Supplier<RuntimeException> exceptionSupplier = () -> new RuntimeException(String.format("Entity's %s was marked for deletion, but it's not in the batching system", entityId));
        List<BatchGroupBatchTouple> touples = getEntityBatchGroupBatchTouples(entityId).orElseThrow(exceptionSupplier);

        for (BatchGroupBatchTouple touple : touples) {
            groups.get(touple.batchGroupID)
                    .getBatch(touple.batchID)
                    .orElseThrow(() -> new RuntimeException(String.format("Batch %d was not found in BatchGroup %d", touple.batchID, touple.batchGroupID)))
                    .removeEntity(entityId);
        }

        entityBatchGroupBatchIDMap.remove(entityId);
        materialChanges.remove(entityId);
        meshChanges.remove(entityId);
        transformationChanges.remove(entityId);
    }

    public Map<Integer, MaterialBatchGroup> getGroups() {
        return groups;
    }

    private void processGPUDataChanges() {
        if (!newBatchDataQueue.isEmpty()) {
            LOGGER.trace("Processing new Batches' data...");
            for (MeshMaterialBatch batch : newBatchDataQueue) {
                Engine.gpuDataStore.uploadBatchDataToGPU(batch);
                Engine.aabbGpuDataStore.uploadBatchDataToGPU(batch);
            }
            newBatchDataQueue.clear();
            LOGGER.trace("New Batches' data processed");
        }

        if (!changedBatchDataQueue.isEmpty()) {
            LOGGER.trace("Processing changed Batches' data...");
            for (MeshMaterialBatch batch : changedBatchDataQueue) {
                Engine.gpuDataStore.updateBatchDataInGPU(batch);
                Engine.aabbGpuDataStore.updateBatchDataInGPU(batch);
                batch.dataUpdated();
            }
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
                    Engine.gpuDataStore.updateBatchDataInGPU(batchEntry.getValue());
                    Engine.aabbGpuDataStore.updateBatchDataInGPU(batchEntry.getValue());
                    batchEntry.getValue().dataUpdated();
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
        for (Integer entityId : transformationChanges.keySet()) {
            Supplier<RuntimeException> exceptionSupplier = () -> new RuntimeException(String.format("Entity's %s transform was changed, but it's not in the batching system", entityId));
            List<BatchGroupBatchTouple> touples = getEntityBatchGroupBatchTouples(entityId).orElseThrow(exceptionSupplier);

            for (BatchGroupBatchTouple touple : touples) {
                MeshMaterialBatch batch = groups.get(touple.batchGroupID)
                        .getBatch(touple.batchID)
                        .orElseThrow(() -> new RuntimeException(String.format("Batch %d was not found in BatchGroup %d", touple.batchID, touple.batchGroupID)));

                if (newBatchDataQueue.contains(batch)) {
                    LOGGER.trace(
                            "BatchGroup {} Batch {} data won't be updated as it's still waiting for data uploading",
                            touple.batchGroupID,
                            touple.batchID
                    );
                } else {
                    changedBatchDataQueue.add(batch);
                }
            }
        }
        transformationChanges.clear();
        LOGGER.trace("Transformation changes are applied");
    }

    private void processMeshChanges() {
        if (meshChanges.isEmpty()) {
            return;
        }

        LOGGER.trace("Processing mesh changes...");
        for (Map.Entry<Integer, MeshMaterialPair> entry : materialChanges.entrySet()) {
            int entityId = entry.getKey();
            MeshMaterialPair meshMaterialPair = entry.getValue();
            LOGGER.trace("{}'s mesh has changed", entityId);

            Supplier<RuntimeException> exceptionSupplier = () -> new RuntimeException(String.format("Entity %s mesh was changed, but it's not in the batching system", entityId));
            List<BatchGroupBatchTouple> touples = getEntityBatchGroupBatchTouples(entityId).orElseThrow(exceptionSupplier);
            entityBatchGroupBatchIDMap.get(entityId).clear();
            for (BatchGroupBatchTouple touple : touples) {
                groups.get(touple.batchGroupID)
                        .getBatch(touple.batchID)
                        .orElseThrow(() -> new RuntimeException(String.format("Batch %d was not found in BatchGroup %d", touple.batchID, touple.batchGroupID)))
                        .removeEntity(entityId);

                add(entityId, meshMaterialPair);
            }
        }

        LOGGER.trace("Mesh changes are applied");
        meshChanges.clear();
    }

    private void processMaterialChanges() {
        if (materialChanges.isEmpty()) {
            return;
        }

        LOGGER.trace("Processing material changes...");
        for (Map.Entry<Integer, MeshMaterialPair> entry : materialChanges.entrySet()) {
            int entityId = entry.getKey();
            MeshMaterialPair meshMaterialPair = entry.getValue();

            Supplier<RuntimeException> exceptionSupplier = () -> new RuntimeException(String.format("Entity's %s material was changed, but it's not in the batching system", entityId));
            List<BatchGroupBatchTouple> touples = getEntityBatchGroupBatchTouples(entityId).orElseThrow(exceptionSupplier);
            entityBatchGroupBatchIDMap.get(entityId).clear();

            for (BatchGroupBatchTouple touple : touples) {
                LOGGER.trace("{}'s material has changed, removing from BatchGroup {} Batch {}, and adding to BatchGroup {} Batch {}", entityId, touple.batchGroupID, touple.batchID, meshMaterialPair.getMaterial().hashCode(), meshMaterialPair.getMesh().hashCode());

                groups.get(touple.batchGroupID)
                        .getBatch(touple.batchID)
                        .orElseThrow(() -> new RuntimeException(String.format("Batch %d was not found in BatchGroup %d", touple.batchID, touple.batchGroupID)))
                        .removeEntity(entityId);

                add(entityId, meshMaterialPair);
            }
        }

        materialChanges.clear();
        LOGGER.trace("Material changes are applied");
    }

    private Optional<List<BatchGroupBatchTouple>> getEntityBatchGroupBatchTouples(int entityId) {
        return Optional.ofNullable(entityBatchGroupBatchIDMap.getOrDefault(entityId, null));
    }
}
