package spck.engine.render;

public interface Renderer {
    void uploadBatchDataToGPU(Batch batch);

    void updateBatchDataInGPU(Batch batch);

    void render(Batch batch);
}
