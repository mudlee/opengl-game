package spck.engine.ecs.ui;

import spck.engine.ecs.Entity;

import java.util.Objects;

public class UICanvasEntity extends Entity {
    protected final UICanvasScaler canvasScaler;
    protected UICanvasComponent canvasComponent;

    public UICanvasEntity(UICanvasScaler canvasScaler) {
        this.canvasScaler = canvasScaler;
    }

    @Override
    public void onEntityCreated() {
        canvasComponent = addComponent(UICanvasComponent.class);
        canvasComponent.setCanvasScaler(canvasScaler);
    }

    public void addImage(UIImage image) {
        Objects.requireNonNull(canvasComponent).addImage(image);
    }

    public void addText(UIText text) {
        Objects.requireNonNull(canvasComponent).addText(text);
    }
}
