package spck.engine.ecs.render.components;

import spck.engine.ecs.StateAwareComponent;
import spck.engine.render.MeshMaterialCollection;
import spck.engine.render.Transform;

public class RenderComponent extends StateAwareComponent {
    public MeshMaterialCollection meshMaterialCollection;
    public Transform transform = new Transform();
}
