package spck.game;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;
import spck.engine.Engine;
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

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class GameCameraController extends UICanvasEntity {
    private static final Map<MoveDirection, Integer> moveKeyMap = new HashMap<>();
    private static final float ACCELERATION = 3f;
    private static final float MOVE_SPEED = 3f;
    private static final float SCROLL_SPEED = 3f;
    private static final Vector3f REUSABLE_3D_VECTOR = new Vector3f();
    private static final Vector2f REUSABLE_2D_VECTOR = new Vector2f();

    private enum CursorTextureRegistryID implements TextureRegistryID {
        CURSOR
    }

    static {
        moveKeyMap.put(MoveDirection.LEFT, GLFW_KEY_A);
        moveKeyMap.put(MoveDirection.RIGHT, GLFW_KEY_D);
        moveKeyMap.put(MoveDirection.UPWARD, GLFW_KEY_W);
        moveKeyMap.put(MoveDirection.DOWNWARD, GLFW_KEY_S);
    }

    private final GameCamera camera;
    private final Vector3f moveTarget;
    private final Vector2f zoomTarget;

    public GameCameraController(GameCamera camera) {
        this.camera = camera;
        moveTarget = new Vector3f(camera.getPosition());
        zoomTarget = new Vector2f(camera.getSize(), 0);

        for (Map.Entry<MoveDirection, Integer> entry : moveKeyMap.entrySet()) {
            Input.onKeyHeldDown(entry.getValue(), event -> move(entry.getKey()));
        }

        Input.onMouseScroll(event -> {
            if (event.offset.y > 0) {
                move(MoveDirection.FORWARD);
            } else {
                move(MoveDirection.BACKWARD);
            }
        });

        MessageBus.register(LifeCycle.UPDATE.eventID(), () -> {
            if (moveTarget.distance(camera.getPosition()) > 0.01f) {
                REUSABLE_3D_VECTOR.set(camera.getPosition());
                REUSABLE_3D_VECTOR.lerp(moveTarget, Time.deltaTime * ACCELERATION);
                camera.setPosition(REUSABLE_3D_VECTOR);
            }
            if (Math.abs(zoomTarget.x - camera.getSize()) > 0.01f) {
                REUSABLE_2D_VECTOR.set(camera.getSize(), 0);
                REUSABLE_2D_VECTOR.lerp(zoomTarget, Time.deltaTime * ACCELERATION);
                camera.setSize(REUSABLE_2D_VECTOR.x);
            }
        });
    }

    @Override
    public void onEntityCreated() {
        super.onEntityCreated();

        Engine.window.captureMouse();

        Texture2D texture2D = TextureStorage.loadFromResource("/textures/pointer.png", CursorTextureRegistryID.CURSOR);
        TextureRegistry.register(texture2D);
        UIImage image = UIImage.build(texture2D.getId(), 30, 30);
        canvasComponent.addImage(image);

        Vector2d mousePos = Input.getMouseAbsolutePosition();
        image.setPosition((int) mousePos.x, (int) mousePos.y);

        Input.onMouseMove(event -> {
            move(event.direction);
            image.setPosition((int) event.relativePosition.x, (int) event.relativePosition.y);
        });
    }

    private void move(MoveDirection direction) {
        switch (direction) {
            case FORWARD:
                zoomTarget.set(camera.getSize() + SCROLL_SPEED);
                break;
            case BACKWARD:
                zoomTarget.set(camera.getSize() - SCROLL_SPEED);
                break;
            case LEFT:
                moveTarget.set(camera.getPosition().x - MOVE_SPEED, camera.getPosition().y, camera.getPosition().z);
                break;
            case RIGHT:
                moveTarget.set(camera.getPosition().x + MOVE_SPEED, camera.getPosition().y, camera.getPosition().z);
                break;
            case UPWARD:
                moveTarget.set(camera.getPosition().x, camera.getPosition().y + MOVE_SPEED, camera.getPosition().z);
                break;
            case DOWNWARD:
                moveTarget.set(camera.getPosition().x, camera.getPosition().y - MOVE_SPEED, camera.getPosition().z);
                break;
        }
    }
}