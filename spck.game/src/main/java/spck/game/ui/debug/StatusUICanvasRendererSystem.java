package spck.game.ui.debug;

import com.artemis.ComponentMapper;
import spck.engine.ecs.ui.UICanvasRendererSystemCore;
import spck.engine.framework.UIRenderer;
import spck.engine.window.GLFWWindow;

public class StatusUICanvasRendererSystem extends UICanvasRendererSystemCore {
    private ComponentMapper<StatusUICanvasComponent> canvasComponents;

    public StatusUICanvasRendererSystem(UIRenderer uiRenderer, GLFWWindow window) {
        super(window,uiRenderer, StatusUICanvasComponent.class);
    }

    @Override
    protected void processSystem() {
        run(canvasComponents);
    }
}
