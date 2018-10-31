package spck.engine.ecs.ui;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import spck.engine.Antialiasing;
import spck.engine.Engine;
import spck.engine.framework.Graphics;
import spck.engine.framework.UIRenderer;
import spck.engine.util.RunOnce;

public class UIRendererSystem extends BaseEntitySystem {
    private final UIRenderer uiRenderer = new UIRenderer(Engine.preferences.defaultFont);
    private ComponentMapper<UITextComponent> textComponents;
    private ComponentMapper<UIImageComponent> imageComponents;

    public UIRendererSystem() {
        super(Aspect.one(UITextComponent.class, UIImageComponent.class));
    }

    @Override
    protected void processSystem() {
        RunOnce.run("UIRendererSystem init", () -> uiRenderer.init(Engine.window.getPreferences().getAntialiasing() != Antialiasing.OFF));

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
            if (textComponents.has(ids[i])) {
                UITextComponent component = textComponents.get(ids[i]);
                updateScreenScaleFactorIfNeeded(component);
                uiRenderer.renderText(component);
            } else if (imageComponents.has(ids[i])) {
                UIImageComponent component = imageComponents.get(ids[i]);
                updateScreenScaleFactorIfNeeded(component);
                uiRenderer.renderImage(component);
            }
        }

        uiRenderer.endFrame();

        if (Engine.preferences.polygonRenderMode) {
            Graphics.setPolygonMode(Graphics.PolygonMode.LINE);
        }
    }

    private void updateScreenScaleFactorIfNeeded(UIComponent component) {
        if (component.screenScaleFactor != Engine.window.getPreferences().getScreenScaleFactor()) {
            component.updateScreenCoords(Engine.window.getPreferences().getScreenScaleFactor());
        }
    }
}
