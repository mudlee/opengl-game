package spck.engine.render;

import org.joml.AABBf;
import spck.engine.render.shader.Shader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MeshMaterialCollection {
    private final List<MeshMaterialPair> collection;
    private final List<MeshMaterialPair> meshChanges = new ArrayList<>();
    private final List<MeshMaterialPair> materialChanges = new ArrayList<>();
    private final List<AABBf> aabbs;

    public MeshMaterialCollection(List<MeshMaterialPair> collection) {
        this.collection = Collections.unmodifiableList(collection);
        this.aabbs = collection.stream().map(meshMaterialPair -> meshMaterialPair.getMesh().getAABB()).collect(Collectors.toList());
    }

    public List<MeshMaterialPair> getCollection() {
        return collection;
    }

    public List<Shader> getShaders() {
        return collection.stream().map(MeshMaterialPair::getMaterial).map(Material::getShader).collect(Collectors.toList());
    }

    public List<AABBf> getAABBs() {
        return aabbs;
    }

    public List<MeshMaterialPair> ackMeshChanges() {
        meshChanges.clear();

        for (MeshMaterialPair meshMaterialPair : collection) {
            if (meshMaterialPair.getMesh().isChanged()) {
                meshChanges.add(meshMaterialPair);
                meshMaterialPair.getMesh().ackChanges();
            }
        }

        return meshChanges;
    }

    public List<MeshMaterialPair> ackMaterialChanges() {
        materialChanges.clear();

        for (MeshMaterialPair meshMaterialPair : collection) {
            if (meshMaterialPair.getMaterial().isChanged()) {
                materialChanges.add(meshMaterialPair);
                meshMaterialPair.getMaterial().ackChanges();
            }
        }

        return materialChanges;
    }

    public Optional<Material> findMaterial(Material material) {
        for (MeshMaterialPair meshMaterialPair : collection) {
            if (meshMaterialPair.getMaterial().equals(material)) {
                return Optional.of(meshMaterialPair.getMaterial());
            }
        }

        return Optional.empty();
    }
}
