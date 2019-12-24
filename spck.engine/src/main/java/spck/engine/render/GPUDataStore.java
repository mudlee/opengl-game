package spck.engine.render;

public interface GPUDataStore<T> {
    void uploadDataToGPU(T data);

    void updateDataInGPU(T data);
}
