package spck.engine.ecs.ui;

public class UIImage extends UIElement {
    private int textureId;
    private int width;
    private int height;

    private UIImage() {
    }

    public UIImage build(int textureId, int width, int height) {
        UIImage image = new UIImage();
        image.textureId = textureId;
        image.width = width;
        image.height = height;
        return image;
    }

    public int getTextureId() {
        return textureId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
