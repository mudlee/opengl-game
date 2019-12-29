package spck.game;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;
import spck.engine.MoveDirection;
import spck.engine.Time;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.ecs.AbstractEntity;
import spck.engine.framework.assets.TextureStorage;
import spck.engine.render.camera.OrthoCamera;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureRegistry;
import spck.engine.render.textures.TextureRegistryID;
import spck.engine.ui.Canvas;
import spck.engine.ui.Image;
import spck.engine.window.GLFWWindow;
import spck.engine.window.Input;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class GameCameraController extends AbstractEntity {
    private static final Map<MoveDirection, Integer> moveKeyMap = new HashMap<>();
    private static final float ACCELERATION = 3f;
    private static final float MOVE_SPEED = 3f;
    private static final float SCROLL_SPEED = 3f;
    private static final float MIN_ZOOM = 4f;
    private static final float MAX_ZOOM = 30f;
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

    private final OrthoCamera camera;
    private final Vector3f moveTarget;
    private final Vector2f zoomTarget;
    private final Canvas canvas;
    private final GLFWWindow window;
    private final Input input;

    GameCameraController(OrthoCamera camera, GLFWWindow window, Input input, Canvas canvas) {
        this.camera = camera;
        this.window = window;
        this.input = input;
        moveTarget = new Vector3f(camera.getPosition());
        zoomTarget = new Vector2f(camera.getSize(), 0);
        this.canvas = canvas;

        for (Map.Entry<MoveDirection, Integer> entry : moveKeyMap.entrySet()) {
            input.onKeyHeldDown(entry.getValue(), event -> move(entry.getKey()));
        }

        input.onMouseScroll(event -> {
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
                if (REUSABLE_2D_VECTOR.x > MIN_ZOOM && REUSABLE_2D_VECTOR.x < MAX_ZOOM) {
                    camera.setSize(REUSABLE_2D_VECTOR.x);
                }
            }
        });
    }

    @Override
    public void onEntityReady() {
        window.captureMouse();

        Texture2D cursorTexture = TextureStorage.loadFromResource("/textures/pointer.png", CursorTextureRegistryID.CURSOR);
        TextureRegistry.register(cursorTexture);

        Vector2d mousePos = input.getMouseAbsolutePosition();

        Image cursor = Image.Builder
                .create(cursorTexture.getId())
                .withX((int) mousePos.x)
                .withY((int) mousePos.y)
                .withWidth(30)
                .withHeight(30)
                .build();
        canvas.addImage(cursor, 1);

        input.onMouseMove(event -> {
            move(event.direction);
            cursor.setX((int) event.relativePosition.x);
            cursor.setY((int) event.relativePosition.y);
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
