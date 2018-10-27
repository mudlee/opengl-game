package spck.engine.debug.ecs;

import spck.engine.graphics.UIObjectPosition;
import spck.engine.ui.ecs.UITextEntity;

public class StatUITextEntity extends UITextEntity {
    public StatUITextEntity(StatUITextComponent.Type type, String text, UIObjectPosition position) {
        super(text, position);

        StatUITextComponent component = addComponent(StatUITextComponent.class);
        component.type = type;
    }
}
