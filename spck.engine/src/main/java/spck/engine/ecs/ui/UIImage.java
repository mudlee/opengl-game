package spck.engine.ecs.ui;

import spck.engine.Engine;

public class UIImage extends UIElement {
    private int textureId;
    private int width;
    private int height;

    private UIImage() {
    }

    public static UIImage build(int textureId, int width, int height) {
        UIImage image = new UIImage();
        image.textureId = textureId;
        image.width = width;
        image.height = height;
        image.setScreenScaleFactor(Engine.window.getPreferences().getScreenScaleFactor());
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
