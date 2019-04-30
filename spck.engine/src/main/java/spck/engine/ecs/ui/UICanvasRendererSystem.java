package spck.engine.ecs.ui;

import com.artemis.ComponentMapper;
import spck.engine.framework.UIRenderer;

public class UICanvasRendererSystem extends UICanvasRendererSystemCore {
    private ComponentMapper<UICanvasComponent> canvasComponents;

    public UICanvasRendererSystem(UIRenderer uiRenderer) {
        super(uiRenderer, UICanvasComponent.class);
    }

    @Override
    protected void processSystem() {
        run(canvasComponents);
    }
}
