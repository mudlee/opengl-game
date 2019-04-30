package spck.engine.ecs.ui;

import spck.engine.ecs.Entity;

public class UICanvasEntity extends Entity {
    protected final UICanvasScaler canvasScaler;
    protected UICanvasComponent canvasComponent;

    public UICanvasEntity(UICanvasScaler canvasScaler) {
        this.canvasScaler = canvasScaler;
    }

    @Override
    public void onInit() {
        canvasComponent = addComponent(UICanvasComponent.class);
    }

    public UICanvasComponent getCanvasComponent() { // TODO nem a legszebb igy
        if (canvasComponent == null) {
            throw new RuntimeException("Please call Entity.create() before calling this method.");
        }

        return canvasComponent;
    }
}
