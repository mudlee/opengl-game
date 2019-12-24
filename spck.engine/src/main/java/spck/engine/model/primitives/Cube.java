package spck.engine.model.primitives;

import spck.engine.ecs.AbstractEntity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.model.ModelLoader;
import spck.engine.render.MeshMaterialCollection;
import spck.engine.render.Transform;

public class Cube extends AbstractEntity {
    @Override
    public void onEntityReady() {
        MeshMaterialCollection collection = ModelLoader.load("/models/primitives/cube.obj");

        RenderComponent component = addComponent(RenderComponent.class);
        component.meshMaterialCollection = collection;
        component.transform = new Transform();
    }
}

