package spck.engine.ecs.render.components;

import spck.engine.ecs.StateAwareComponent;
import spck.engine.render.Material;
import spck.engine.render.Mesh;
import spck.engine.render.Transform;

public class RenderComponent extends StateAwareComponent {
    public Mesh mesh;
    public Material material;
    public Transform transform;
}
