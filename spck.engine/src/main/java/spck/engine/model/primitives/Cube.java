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
        // TODO
        component.material = model.getParts().get(0).getMaterial();
        component.mesh = model.getParts().get(0).getMesh();
        component.transform = new Transform();
    }
}

