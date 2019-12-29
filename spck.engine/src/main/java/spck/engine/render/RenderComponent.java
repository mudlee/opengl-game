package spck.engine.render;

import spck.engine.ecs.StateAwareComponent;

public class RenderComponent extends StateAwareComponent {
    public MeshMaterialCollection meshMaterialCollection;
    public Transform transform = new Transform();
}
