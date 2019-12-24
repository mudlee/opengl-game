package spck.engine.ecs.ui;

import spck.engine.ecs.AbstractEntity;

import java.util.Objects;

public class UICanvasEntity extends AbstractEntity {
    protected UICanvasComponent canvasComponent;

    @Override
    public void onEntityReady() {
        canvasComponent = addComponent(UICanvasComponent.class);
    }

    public void addImage(UIImage image) {
        Objects.requireNonNull(canvasComponent).addImage(image);
    }

    public void addText(UIText text) {
        Objects.requireNonNull(canvasComponent).addText(text);
    }
}
