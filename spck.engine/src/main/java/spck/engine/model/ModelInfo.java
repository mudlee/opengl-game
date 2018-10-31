package spck.engine.model;

import spck.engine.render.Material;
import spck.engine.render.Mesh;

public class ModelInfo {
    private final Mesh mesh;
    private final Material material;

    ModelInfo(Mesh mesh, Material material) {
        this.mesh = mesh;
        this.material = material;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Material getMaterial() {
        return material;
    }
}
