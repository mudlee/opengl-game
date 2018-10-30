package spck.engine.ecs.debug;

import spck.engine.ecs.ui.UITextEntity;
import spck.engine.ui.UIObjectPosition;

class StatUITextEntity extends UITextEntity {
    StatUITextEntity(StatUITextComponent.Type type, String text, UIObjectPosition position) {
        super(text, position);

        StatUITextComponent component = addComponent(StatUITextComponent.class);
        component.type = type;
    }
}
