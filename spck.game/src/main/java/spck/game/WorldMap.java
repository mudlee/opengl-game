package spck.game;

import org.joml.AABBf;
import org.joml.Vector3f;
import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.model.ModelLoader;
import spck.engine.physics.PhysicsEntity;
import spck.engine.render.MeshMaterialCollection;

import java.util.List;

public class WorldMap extends Entity implements PhysicsEntity {
    private MeshMaterialCollection collection;

    @Override
    public void onEntityCreated() {
        collection = ModelLoader.load("/models/environment/WorldMap.obj");

        RenderComponent component = addComponent(RenderComponent.class);
        component.meshMaterialCollection = collection;
        component.transform.setRotation(new Vector3f(90, 0, 0));
        component.transform.setScale(new Vector3f(10, 1, 10));
        component.transform.setPosition(new Vector3f(0.07f, 5.7f, 0));
    }

    @Override
    public List<AABBf> getAABBs() {
        return collection.getAABBs();
    }
}
