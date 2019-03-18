package spck.engine.ecs.ui;

import spck.engine.Engine;
import spck.engine.ecs.Entity;
import spck.engine.ui.UIObjectPosition;

public class UITextEntity extends Entity {
    private final String text;
    private final UIObjectPosition position;
    private UITextComponent textComponent;

    public UITextEntity(String text, UIObjectPosition position) {
        this.text = text;
        this.position = position;
    }

    @Override
    public void onInit() {
        textComponent = addComponent(UITextComponent.class);
        textComponent.text = text;
        textComponent.position = position;
        textComponent.screenOffset.set(0, (int) textComponent.size);
        textComponent.updateScreenCoords(Engine.window.getPreferences().getScreenScaleFactor());
    }
}
