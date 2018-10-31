package spck.engine.ecs.render.components;

import com.artemis.Component;
import spck.engine.render.Material;
import spck.engine.render.Mesh;
import spck.engine.render.Transform;

public class RenderComponent extends Component {
    public Mesh mesh;
    public Material material;
    public Transform transform;
}
