package spck.game;

import org.joml.AABBf;
import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.model.ModelLoader;
import spck.engine.physics.PhysicsEntity;
import spck.engine.render.MeshMaterialCollection;
import spck.engine.render.Transform;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Castle extends Entity implements PhysicsEntity {
    private final List<AABBf> aabbs = new ArrayList<>();

    @Override
    public void onEntityCreated() {
        MeshMaterialCollection collection = ModelLoader.load("/models/environment/castle/castle.obj");

        RenderComponent component = addComponent(RenderComponent.class);
        component.meshMaterialCollection = collection;
        component.transform = new Transform();

        aabbs.addAll(collection.getCollection().stream().map(meshMaterialPair -> meshMaterialPair.getMesh().getAABB()).collect(Collectors.toList()));
    }

    @Override
    public List<AABBf> getAABBs() {
        return aabbs;
    }
}
