package spck.game;

import org.joml.Vector2f;
import org.lwjgl.nanovg.NSVGImage;
import spck.engine.Input.Input;
import spck.engine.MoveDirection;
import spck.engine.Time;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UIImage;
import spck.engine.framework.assets.TextureStorage;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureRegistry;
import spck.engine.render.textures.TextureRegistryID;
import spck.engine.ui.svg.SVGLoader;

public class Map extends UICanvasEntity {


    private enum MapTextureRegistryID implements TextureRegistryID {
        MAP;
    }

    private static final float MOVE_SPEED = 100f;

    private final GameCamera camera;
    private UIImage image;
    private final Vector2f moveTarget;
    private static final Vector2f REUSABLE_2D_VECTOR = new Vector2f();
    private static final float ACCELERATION = 3f;

    public Map(GameCamera camera) {
        this.camera = camera;
        moveTarget = new Vector2f(camera.getPosition().x, camera.getPosition().y);
    }

    @Override
    public void onEntityCreated() {
        super.onEntityCreated();

        NSVGImage svg = SVGLoader.load("/textures/world.svg");
        Texture2D texture2D = TextureStorage.loadFromSVG(svg, MapTextureRegistryID.MAP);
        TextureRegistry.register(texture2D);
        image = UIImage.build(texture2D.getId(), texture2D.getWidth(), texture2D.getHeight());
        image.setPosition(
                0,
                0
        );
        canvasComponent.addImage(image);
        Input.onMouseMove(event -> {
            move(event.direction);
        });

        Input.onMouseScroll(event -> {
            if (event.offset.y > 0) {
            } else {
            }
        });

        MessageBus.register(LifeCycle.UPDATE.eventID(), () -> {
            float distance = moveTarget.distance(image.getPosition().get().x, image.getPosition().get().y);
            if (distance > 0.01f) {
                REUSABLE_2D_VECTOR.set(image.getPosition().get().x, image.getPosition().get().y);
                REUSABLE_2D_VECTOR.lerp(moveTarget, Time.deltaTime * ACCELERATION);
                image.setPosition(REUSABLE_2D_VECTOR.x, REUSABLE_2D_VECTOR.y);
            }
        });
    }

    private void move(MoveDirection direction) {
        switch (direction) {
            case LEFT:
                moveTarget.set(image.getPosition().get().x + MOVE_SPEED, image.getPosition().get().y);
                break;
            case RIGHT:
                moveTarget.set(image.getPosition().get().x - MOVE_SPEED, image.getPosition().get().y);
                break;
            case UPWARD:
                moveTarget.set(image.getPosition().get().x, image.getPosition().get().y + MOVE_SPEED);
                break;
            case DOWNWARD:
                moveTarget.set(image.getPosition().get().x, image.getPosition().get().y - MOVE_SPEED);
                break;
        }
    }
}
