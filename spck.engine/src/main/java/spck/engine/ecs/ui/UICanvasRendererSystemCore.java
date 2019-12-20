package spck.engine.ecs.ui;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import spck.engine.Engine;
import spck.engine.framework.Graphics;
import spck.engine.framework.UIRenderer;
import spck.engine.util.RunOnce;
import spck.engine.window.Antialiasing;
import spck.engine.window.GLFWWindow;

public abstract class UICanvasRendererSystemCore extends BaseEntitySystem {
    private final GLFWWindow window;
    private final UIRenderer uiRenderer;

    public UICanvasRendererSystemCore(GLFWWindow window, UIRenderer uiRenderer, Class<? extends Component> componentClass) {
        super(Aspect.one(componentClass));
        this.window = window;
        this.uiRenderer = uiRenderer;
    }

    @Override
    protected void processSystem() {
    }

    protected void run(ComponentMapper<? extends UICanvasComponent> canvasComponents) {
        RunOnce.run("Canvas rendering init", () -> uiRenderer.init(window.getAntialiasing() != Antialiasing.OFF));

        IntBag actives = subscription.getEntities();
        int[] ids = actives.getData();

        if (actives.size() == 0) {
            return;
        }

        if (Engine.preferences.polygonRenderMode) {
            Graphics.setPolygonMode(Graphics.PolygonMode.FILL);
        }

        uiRenderer.beginFrame(
                window.getWidth(),
                window.getHeight(),
                window.getDevicePixelRatio()
        );

        for (int i = 0, s = actives.size(); s > i; i++) {
            if (canvasComponents.has(ids[i])) {
                UICanvasComponent canvas = canvasComponents.get(ids[i]);
                for (UIText text : canvas.getTexts()) {
                    uiRenderer.renderText(text);
                }

                for (UIImage image : canvas.getImages()) {
                    uiRenderer.renderImage(image);
                }
            }
        }

        uiRenderer.endFrame();

        if (Engine.preferences.polygonRenderMode) {
            Graphics.setPolygonMode(Graphics.PolygonMode.LINE);
        }
    }
}
