package spck.game;

import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.model.ModelLoader;
import spck.engine.render.MeshMaterialCollection;
import spck.engine.render.Transform;

public class Monu9 extends Entity {
    @Override
    public void onEntityCreated() {
        MeshMaterialCollection collection = ModelLoader.load("/models/environment/monu9.obj");

        RenderComponent component = addComponent(RenderComponent.class);
        component.meshMaterialCollection = collection;
        component.transform = new Transform();
    }
}
