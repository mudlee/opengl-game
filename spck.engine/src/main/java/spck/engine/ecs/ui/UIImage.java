package spck.engine.ecs.ui;

import spck.engine.Engine;
import spck.engine.window.GLFWWindow;

public class UIImage extends UIElement {
    private int textureId;
    private int width;
    private int height;
    public Integer handle;

    private UIImage(GLFWWindow window) {
        super(window);
    }

    public static UIImage build(int textureId, int width, int height, GLFWWindow window) {
        UIImage image = new UIImage(window);
        image.textureId = textureId;
        image.width = width * window.getDevicePixelRatio();
        image.height = height * window.getDevicePixelRatio();
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
