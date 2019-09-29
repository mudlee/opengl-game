package spck.engine.render;

public class MeshMaterialPair {
    private final Mesh mesh;
    private final Material material;

    public MeshMaterialPair(Mesh mesh, Material material) {
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
