package spck.game;

import org.joml.Vector2d;
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
import spck.engine.render.camera.Camera;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureRegistry;
import spck.engine.render.textures.TextureRegistryID;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class RPGCameraController extends UICanvasEntity {
    private static final Map<MoveDirection, Integer> moveKeyMap = new HashMap<>();
    private static final float ACCELERATION = 3f;
    private static final float MOVE_SPEED = 3f;
    private static final float SCROLL_SPEED = 3f;
    private static final Vector3f REUSABLE_VECTOR = new Vector3f();
    private static final double MOUSE_SENSITIVITY = 30f;
    private final Vector3f REUSABLE_UP_VECTOR = new Vector3f(0, 1, 0);

    private enum CursorTextureRegistryID implements TextureRegistryID {
        CURSOR
    }

    static {
        moveKeyMap.put(MoveDirection.LEFT, GLFW_KEY_A);
        moveKeyMap.put(MoveDirection.RIGHT, GLFW_KEY_D);
        moveKeyMap.put(MoveDirection.FORWARD, GLFW_KEY_W);
        moveKeyMap.put(MoveDirection.BACKWARD, GLFW_KEY_S);
    }

    private final Camera camera;
    private final Vector3f moveTarget;

    public RPGCameraController(Camera camera) {
        this.camera = camera;
        moveTarget = new Vector3f(camera.getPosition());

        for (Map.Entry<MoveDirection, Integer> entry : moveKeyMap.entrySet()) {
            Input.onKeyHeldDown(entry.getValue(), event -> move(entry.getKey()));
        }

        Input.onMouseScroll(event -> {
            if (event.offset.y > 0) {
                move(MoveDirection.UPWARD);
            } else {
                move(MoveDirection.DOWNWARD);
            }
        });

        MessageBus.register(LifeCycle.UPDATE.eventID(), () -> {
            if (moveTarget.distance(camera.getPosition()) > 0.01f) {
                REUSABLE_VECTOR.set(camera.getPosition());
                REUSABLE_VECTOR.lerp(moveTarget, Time.deltaTime * ACCELERATION);
                camera.setPosition(REUSABLE_VECTOR);
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

        Vector2d mousePos = Input.getMousePosition();
        image.setPosition((int) mousePos.x, (int) mousePos.y);

        Input.onMouseMove(event -> {
            int newX = (int) ((double) image.getPosition().get().x + event.offset.x * MOUSE_SENSITIVITY);
            int newY = (int) ((double) image.getPosition().get().y - event.offset.y * MOUSE_SENSITIVITY);

            if (newX < 0) {
                newX = image.getPosition().get().x;
                move(MoveDirection.LEFT);
            } else if (newX > (Engine.window.getPreferences().getWidth() - 50)) {
                newX = image.getPosition().get().x;
                move(MoveDirection.RIGHT);
            }

            if (newY < 0) {
                newY = image.getPosition().get().y;
                move(MoveDirection.FORWARD);
            } else if (newY > (Engine.window.getPreferences().getHeight() - 50)) {
                newY = image.getPosition().get().y;
                move(MoveDirection.BACKWARD);
            }

            image.setPosition(newX, newY);
        });
    }

    private void move(MoveDirection direction) {
        switch (direction) {
            case FORWARD:
                REUSABLE_VECTOR.set(camera.getFrontVector());
                REUSABLE_VECTOR.y = 0;
                moveTarget.set(camera.getPosition()).add(REUSABLE_VECTOR.mul(MOVE_SPEED));
                break;
            case BACKWARD:
                REUSABLE_VECTOR.set(camera.getFrontVector());
                REUSABLE_VECTOR.y = 0;
                moveTarget.set(camera.getPosition()).add(REUSABLE_VECTOR.mul(-MOVE_SPEED));
                break;
            case LEFT:
                REUSABLE_VECTOR.set(camera.getFrontVector());
                moveTarget.set(camera.getPosition()).add(REUSABLE_VECTOR.cross(REUSABLE_UP_VECTOR).normalize().mul(-MOVE_SPEED));
                break;
            case RIGHT:
                REUSABLE_VECTOR.set(camera.getFrontVector());
                moveTarget.set(camera.getPosition()).add(REUSABLE_VECTOR.cross(REUSABLE_UP_VECTOR).normalize().mul(MOVE_SPEED));
                break;
            case UPWARD:
                moveTarget.set(camera.getPosition().x, camera.getPosition().y + SCROLL_SPEED, camera.getPosition().z);
                break;
            case DOWNWARD:
                moveTarget.set(camera.getPosition().x, camera.getPosition().y - SCROLL_SPEED, camera.getPosition().z);
                break;
        }
    }
}
