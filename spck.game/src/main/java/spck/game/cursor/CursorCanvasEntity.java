package spck.game.cursor;

import spck.engine.bus.MessageBus;
import spck.engine.bus.MouseEvent;
import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UICanvasScaler;
import spck.engine.ecs.ui.UIImage;
import spck.engine.framework.assets.TextureStorage;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureRegistry;
import spck.engine.render.textures.TextureRegistryID;

public class CursorCanvasEntity extends UICanvasEntity {
    public CursorCanvasEntity(UICanvasScaler canvasScaler) {
        super(canvasScaler);
    }

    private enum x implements TextureRegistryID {
        A
    }

    @Override
    public void onEntityCreated() {
        super.onEntityCreated();

        Texture2D texture2D = TextureStorage.loadFromResource("/textures/pointer.png", x.A);
        TextureRegistry.register(texture2D);
        UIImage image = UIImage.build(texture2D.getId(), 19, 19);
        canvasComponent.addImage(image);

        MessageBus.register(MouseEvent.MOVE, event -> {
            MouseEvent m = (MouseEvent) event;
            //image.getScreenCoords().set(m.getMousePosition());
            image.getPosition().setLeft((int) m.getMousePosition().x);
            image.getPosition().setTop((int) m.getMousePosition().y);
            image.updateScreenCoords();
        });
    }
}
