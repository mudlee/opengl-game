package spck.engine.ui.ecs;

import spck.engine.ecs.Entity;
import spck.engine.graphics.UIObjectPosition;

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
