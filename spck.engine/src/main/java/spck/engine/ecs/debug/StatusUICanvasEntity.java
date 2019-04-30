package spck.engine.ecs.debug;

import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UICanvasScaler;

public class StatusUICanvasEntity extends UICanvasEntity {
    StatusUICanvasEntity(UICanvasScaler canvasScaler) {
        super(canvasScaler);
    }

    @Override
    public void onEntityCreated() {
        canvasComponent = addComponent(StatusUICanvasComponent.class);
        canvasComponent.setCanvasScaler(canvasScaler);
    }
}
