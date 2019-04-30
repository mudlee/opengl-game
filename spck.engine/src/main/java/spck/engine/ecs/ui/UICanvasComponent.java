package spck.engine.ecs.ui;

import spck.engine.ecs.ECSComponent;

import java.util.ArrayList;
import java.util.List;

public class UICanvasComponent extends ECSComponent {
    private List<UIImage> images = new ArrayList<>();
    private List<UIText> texts = new ArrayList<>();
    private UICanvasScaler canvasScaler;

    public UICanvasScaler getCanvasScaler() {
        return canvasScaler;
    }

    public void setCanvasScaler(UICanvasScaler canvasScaler) {
        this.canvasScaler = canvasScaler;
    }

    public void addImage(UIImage image) {
        images.add(image);
    }

    public void addText(UIText text) {
        texts.add(text);
    }

    public List<UIImage> getImages() {
        return images;
    }

    public List<UIText> getTexts() {
        return texts;
    }
}
