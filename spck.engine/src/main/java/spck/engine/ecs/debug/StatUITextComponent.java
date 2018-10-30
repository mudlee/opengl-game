package spck.engine.ecs.debug;

import com.artemis.Component;

@SuppressWarnings("WeakerAccess")
public class StatUITextComponent extends Component {
    public Type type;

    public enum Type {
        FPS,
        VSYNC,
        RENDER_TIME,
        CAM_POS,
        CAM_ROT,
        VERTS,
        VERTS_TOTAL,
        BATCH_GROUPS,
        BATCHES
    }
}
