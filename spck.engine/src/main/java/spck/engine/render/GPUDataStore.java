package spck.engine.render;

public interface GPUDataStore {
    void uploadBatchDataToGPU(MeshMaterialBatch batch);

    void updateBatchDataInGPU(MeshMaterialBatch batch);
}
