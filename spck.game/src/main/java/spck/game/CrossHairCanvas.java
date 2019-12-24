package spck.game;

import spck.engine.framework.assets.TextureStorage;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureRegistry;
import spck.engine.render.textures.TextureRegistryID;
import spck.engine.ui.Canvas;
import spck.engine.ui.Image;
import spck.engine.window.GLFWWindow;

public class CrossHairCanvas extends Canvas {
    private final GLFWWindow window;

    private enum CrossHairTextureRegistryID implements TextureRegistryID {
        CROSSHAIR
    }

    public CrossHairCanvas(GLFWWindow window) {
        this.window = window;
    }

    @Override
    public void onEntityReady() {
        super.onEntityReady();

        Texture2D texture2D = TextureStorage.loadFromResource("/ui/crosshair.png", CrossHairTextureRegistryID.CROSSHAIR);
        TextureRegistry.register(texture2D);
        int pixelRatio = window.getDevicePixelRatio();
        Image image = Image.Builder
                .create(texture2D.getId())
                .withX((int)((float)window.getWindowWidth() / 2f - pixelRatio * 25f))
                .withY((int)((float)window.getWindowHeight() / 2f - pixelRatio * 25f))
                .withWidth(50)
                .withHeight(50)
                .build();
        addImage(image);
    }
}
