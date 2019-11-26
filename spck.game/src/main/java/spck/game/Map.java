package spck.game;

import org.lwjgl.nanovg.NSVGImage;
import spck.engine.Engine;
import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UIImage;
import spck.engine.framework.assets.TextureStorage;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureRegistry;
import spck.engine.render.textures.TextureRegistryID;
import spck.engine.ui.svg.SVGLoader;

public class Map extends UICanvasEntity {
    private enum MapTextureRegistryID implements TextureRegistryID {
        MAP
    }

    @Override
    public void onEntityCreated() {
        super.onEntityCreated();

        NSVGImage svg = SVGLoader.load("/textures/world.svg");
        Texture2D texture2D = TextureStorage.loadFromSVG(svg, MapTextureRegistryID.MAP);
        TextureRegistry.register(texture2D);
        int pixelRatio = Engine.window.getPreferences().getDevicePixelRatio().orElseThrow();
        UIImage image = UIImage.build(texture2D.getId(), Engine.window.getPreferences().getWidth() / pixelRatio, Engine.window.getPreferences().getHeight() / pixelRatio);
        image.setPosition(
                0,
                0
        );
        canvasComponent.addImage(image);
    }
}
