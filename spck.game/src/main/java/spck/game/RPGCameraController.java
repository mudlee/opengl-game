package spck.game;

import org.joml.Vector3f;
import spck.engine.MoveDirection;
import spck.engine.Time;
import spck.engine.bus.KeyEvent;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.bus.MouseEvent;
import spck.engine.render.camera.Camera;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class RPGCameraController {
    private static final Map<MoveDirection, Integer> moveKeyMap = new HashMap<>();
    private static final float ACCELERATION = 3f;
    private static final float MOVE_SPEED = 1f;
    private static final float SCROLL_SPEED = 3f;
    private static final Vector3f REUSABLE_VECTOR = new Vector3f();
    private final Vector3f REUSABLE_UP_VECTOR = new Vector3f(0, 1, 0);

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
            MessageBus.register(KeyEvent.keyHeldDown(entry.getValue()), () -> {
                move(entry.getKey());
            });
        }

        MessageBus.register(MouseEvent.SCROLL, event -> {
            if (((MouseEvent) event).getMouseScrollOffsetVector().y > 0) {
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
