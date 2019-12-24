package spck.game;

import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UIImage;
import spck.engine.framework.assets.TextureStorage;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureRegistry;
import spck.engine.render.textures.TextureRegistryID;
import spck.engine.window.GLFWWindow;

public class CrossHair extends UICanvasEntity {
    private final GLFWWindow window;

    private enum CrossHairTextureRegistryID implements TextureRegistryID {
        CROSSHAIR
    }

    public CrossHair(GLFWWindow window) {
        this.window = window;
    }

    @Override
    public void onEntityReady() {
        super.onEntityReady();

        Texture2D texture2D = TextureStorage.loadFromResource("/ui/crosshair.png", CrossHairTextureRegistryID.CROSSHAIR);
        TextureRegistry.register(texture2D);
        UIImage image = UIImage.build(texture2D.getId(), 50, 50, window);
        int pixelRatio = window.getDevicePixelRatio();
        image.setPosition(
                (float) window.getWidth() / 2f - pixelRatio * 25f,
                (float) window.getHeight() / 2f - pixelRatio * 25f
        );
        canvasComponent.addImage(image);
    }
}
