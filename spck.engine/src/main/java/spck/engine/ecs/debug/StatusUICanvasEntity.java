package spck.engine.ecs.debug;

import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UICanvasScaler;

public class StatusUICanvasEntity extends UICanvasEntity {
    StatusUICanvasEntity(UICanvasScaler canvasScaler) {
        super(canvasScaler);
    }

    @Override
    public void onInit() {
        canvasComponent = addComponent(StatusUICanvasComponent.class);
    }
}
