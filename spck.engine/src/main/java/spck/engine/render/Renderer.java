package spck.engine.render;

public interface Renderer {
    void uploadBatchDataToGPU(MeshMaterialBatch batch);

    void updateBatchDataInGPU(MeshMaterialBatch batch);

    void render(MeshMaterialBatch batch);
}
