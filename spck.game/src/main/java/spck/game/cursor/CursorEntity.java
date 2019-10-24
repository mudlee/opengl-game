package spck.game.cursor;

import spck.engine.Engine;
import spck.engine.bus.MessageBus;
import spck.engine.bus.MouseEvent;
import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UIImage;
import spck.engine.framework.assets.TextureStorage;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureRegistry;
import spck.engine.render.textures.TextureRegistryID;

public class CursorEntity extends UICanvasEntity {
    private enum CursorTextureRegistryID implements TextureRegistryID {
        CURSOR
    }

    @Override
    public void onEntityCreated() {
        super.onEntityCreated();

        Engine.window.captureMouse();

        Texture2D texture2D = TextureStorage.loadFromResource("/textures/pointer.png", CursorTextureRegistryID.CURSOR);
        TextureRegistry.register(texture2D);
        UIImage image = UIImage.build(texture2D.getId(), 19, 19);
        canvasComponent.addImage(image);

        MessageBus.register(MouseEvent.MOVE, event -> {
            MouseEvent m = (MouseEvent) event;
            image.setPosition(
                    (int) m.getMousePosition().x,
                    (int) m.getMousePosition().y
            );
        });
    }
}
