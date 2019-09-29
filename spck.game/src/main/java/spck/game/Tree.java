package spck.game;

import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.model.ModelInfo;
import spck.engine.model.ModelLoader;
import spck.engine.render.Transform;

public class Tree extends Entity {
    @Override
    public void onEntityCreated() {
        ModelInfo model = ModelLoader.load("/models/environment/lowpolytree.obj");

        RenderComponent component = addComponent(RenderComponent.class);
        component.material = model.getParts().get(0).getMaterial();
        component.mesh = model.getParts().get(0).getMesh();
        component.transform = new Transform();
    }
}
