package spck.engine.ecs.ui;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import spck.engine.Antialiasing;
import spck.engine.Engine;
import spck.engine.framework.Graphics;
import spck.engine.framework.UIRenderer;
import spck.engine.util.RunOnce;

public abstract class UICanvasRendererSystemCore extends BaseEntitySystem {
    private final UIRenderer uiRenderer;

    public UICanvasRendererSystemCore(UIRenderer uiRenderer, Class<? extends Component> componentClass) {
        super(Aspect.one(componentClass));
        this.uiRenderer = uiRenderer;
    }

    @Override
    protected void processSystem() {
    }

    protected void run(ComponentMapper<? extends UICanvasComponent> canvasComponents) {
        RunOnce.run("Canvas rendering init", () -> uiRenderer.init(Engine.window.getPreferences().getAntialiasing() != Antialiasing.OFF));

        IntBag actives = subscription.getEntities();
        int[] ids = actives.getData();

        if (actives.size() == 0) {
            return;
        }

        if (Engine.preferences.polygonRenderMode) {
            Graphics.setPolygonMode(Graphics.PolygonMode.FILL);
        }

        uiRenderer.beginFrame(Engine.window.getPreferences().getWidth(), Engine.window.getPreferences().getHeight(), Engine.window.getPreferences().getScreenScaleFactor());

        for (int i = 0, s = actives.size(); s > i; i++) {
            if (canvasComponents.has(ids[i])) {
                UICanvasComponent canvas = canvasComponents.get(ids[i]);
                for (UIText text : canvas.getTexts()) {
                    updateScreenScaleFactorIfNeeded(text);
                    uiRenderer.renderText(text, canvas.getCanvasScaler());
                }

                for (UIImage image : canvas.getImages()) {
                    updateScreenScaleFactorIfNeeded(image);
                    uiRenderer.renderImage(image, canvas.getCanvasScaler());
                }
            }
        }

        uiRenderer.endFrame();

        if (Engine.preferences.polygonRenderMode) {
            Graphics.setPolygonMode(Graphics.PolygonMode.LINE);
        }
    }

    private void updateScreenScaleFactorIfNeeded(UIElement component) {
        if (component.screenScaleFactor != Engine.window.getPreferences().getScreenScaleFactor()) {
            component.setScreenScaleFactor(Engine.window.getPreferences().getScreenScaleFactor());
        }
    }
}
