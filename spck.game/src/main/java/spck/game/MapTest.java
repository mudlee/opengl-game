package spck.game;

import org.joml.Vector2f;
import org.joml.Vector3f;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.ecs.AbstractEntity;
import spck.engine.framework.assets.TextureStorage;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureRegistry;
import spck.engine.ui.Canvas;
import spck.engine.ui.Image;
import spck.engine.window.GLFWWindow;

public class MapTest extends AbstractEntity {
    private final GLFWWindow window;
    private final Canvas canvas;
    private final GameCamera camera;
    private final Vector2f TEXT_POS_TEMP = new Vector2f();
    private final Vector3f POS = new Vector3f(-28.6f, 28.6f, 0);
    private Image map;

    public MapTest(GLFWWindow window, Canvas canvas, GameCamera camera) {
        this.window = window;
        this.canvas = canvas;
        this.camera = camera;
    }

    @Override
    protected void onEntityReady() {
        Texture2D texture2D = TextureStorage.loadFromResource("/Mercator_projection_Square.jpg", TextureId.MAP_TEXTURE);
        TextureRegistry.register(texture2D);
        map = Image.Builder
                .create(texture2D.getId())
                .withWidth(2058)
                .withHeight(2058)
                .build();
        canvas.addImage(map);

        MessageBus.register(LifeCycle.UPDATE.eventID(), this::onUpdate);
    }

    private void onUpdate() {
        if (camera.isViewMatrixChanged()) {
            TEXT_POS_TEMP.set(camera.worldSpaceToScreenSpace(POS));
            map.setX((int) TEXT_POS_TEMP.x);
            map.setY((int) TEXT_POS_TEMP.y);
        }
    }
}
