package spck.engine.ecs.ui;

import com.artemis.ComponentMapper;
import spck.engine.framework.UIRenderer;
import spck.engine.window.GLFWWindow;

public class UICanvasRendererSystem extends UICanvasRendererSystemCore {
    private ComponentMapper<UICanvasComponent> canvasComponents;

    public UICanvasRendererSystem(GLFWWindow window, UIRenderer uiRenderer) {
        super(window, uiRenderer, UICanvasComponent.class);
    }

    @Override
    protected void processSystem() {
        run(canvasComponents);
    }
}
