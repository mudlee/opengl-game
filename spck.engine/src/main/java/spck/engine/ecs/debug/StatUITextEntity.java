package spck.engine.ecs.debug;

import spck.engine.ecs.ui.UITextEntity;
import spck.engine.ui.UIObjectPosition;

class StatUITextEntity extends UITextEntity {
    private final StatUITextComponent.Type type;

    StatUITextEntity(StatUITextComponent.Type type, String text, UIObjectPosition position) {
        super(text, position);
        this.type = type;
    }

    @Override
    public void onInit() {
        super.onInit();
        StatUITextComponent component = addComponent(StatUITextComponent.class);
        component.type = type;
    }
}
