package spck.engine.ecs.ui;

import spck.engine.ecs.Entity;
import spck.engine.ui.UIObjectPosition;

public class UITextEntity extends Entity {
    private final UITextComponent textComponent;

    public UITextEntity(String text, UIObjectPosition position) {
        super();

        textComponent = addComponent(UITextComponent.class);
        textComponent.text = text;
        textComponent.position = position;
        textComponent.screenOffset.set(0, (int) textComponent.size);
        textComponent.updateScreenCoords();
    }
}
