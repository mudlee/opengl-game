package spck.engine.model.primitives;

import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.model.ModelInfo;
import spck.engine.model.ModelLoader;
import spck.engine.render.Transform;

public class Cube extends Entity {
    @Override
    public void onEntityCreated() {
        ModelInfo model = ModelLoader.load("/models/primitives/cube.obj");

        RenderComponent component = addComponent(RenderComponent.class);
        component.material = model.getMaterial();
        component.mesh = model.getMesh();
        component.transform = new Transform();
    }
}

