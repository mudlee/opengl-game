package spck.engine.model;

import java.util.List;

public class ModelInfo {
    private List<ModelPart> parts;

    public ModelInfo(List<ModelPart> parts) {
        this.parts = parts;
    }

    public List<ModelPart> getParts() {
        return parts;
    }
}
