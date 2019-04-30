package spck.engine.ecs.debug;

import com.artemis.ComponentMapper;
import spck.engine.ecs.ui.UICanvasRendererSystemCore;
import spck.engine.framework.UIRenderer;

public class StatusUICanvasRendererSystem extends UICanvasRendererSystemCore {
    private ComponentMapper<StatusUICanvasComponent> canvasComponents;

    public StatusUICanvasRendererSystem(UIRenderer uiRenderer) {
        super(uiRenderer, StatusUICanvasComponent.class);
    }

    @Override
    protected void processSystem() {
        run(canvasComponents);
    }
}
