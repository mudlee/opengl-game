package spck.game;


import org.joml.AABBf;
import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.model.ModelLoader;
import spck.engine.physics.PhysicsEntity;
import spck.engine.render.MeshMaterialCollection;

import java.util.List;

public class Tree extends Entity implements PhysicsEntity {
    private MeshMaterialCollection collection;

    @Override
    public void onEntityCreated() {
        collection = ModelLoader.load("/models/environment/lowpolytree.obj");

        RenderComponent component = addComponent(RenderComponent.class);
        component.meshMaterialCollection = collection;
    }

    @Override
    public List<AABBf> getAABBs() {
        return collection.getAABBs();
    }
}
