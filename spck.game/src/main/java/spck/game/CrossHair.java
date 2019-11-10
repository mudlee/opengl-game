package spck.game;

import spck.engine.Engine;
import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UIImage;
import spck.engine.framework.assets.TextureStorage;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureRegistry;
import spck.engine.render.textures.TextureRegistryID;

public class CrossHair extends UICanvasEntity {
    private enum CrossHairTextureRegistryID implements TextureRegistryID {
        CROSSHAIR
    }

    @Override
    public void onEntityCreated() {
        super.onEntityCreated();

        Texture2D texture2D = TextureStorage.loadFromResource("/ui/crosshair.png", CrossHairTextureRegistryID.CROSSHAIR);
        TextureRegistry.register(texture2D);
        UIImage image = UIImage.build(texture2D.getId(), 50, 50);
        image.setPosition(Engine.window.getPreferences().getWidth() / 2 - 25, Engine.window.getPreferences().getHeight() / 2 - 25);
        canvasComponent.addImage(image);
    }
}
