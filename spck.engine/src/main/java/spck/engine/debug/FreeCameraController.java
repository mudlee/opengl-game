package spck.engine.debug;

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

public class FreeCameraController {
    private static final Map<MoveDirection, Integer> moveKeyMap = new HashMap<>();

    private static final float ACCELERATION = 3f;
    private static final float MOVE_SPEED = 1f;
    private static final float SCROLL_SPEED = 2f;

    static {
        moveKeyMap.put(MoveDirection.LEFT, GLFW_KEY_A);
        moveKeyMap.put(MoveDirection.RIGHT, GLFW_KEY_D);
        moveKeyMap.put(MoveDirection.FORWARD, GLFW_KEY_W);
        moveKeyMap.put(MoveDirection.BACKWARD, GLFW_KEY_S);
        moveKeyMap.put(MoveDirection.UPWARD, GLFW_KEY_SPACE);
        moveKeyMap.put(MoveDirection.DOWNWARD, GLFW_KEY_LEFT_CONTROL);
    }

    private final Vector3f rotation;
    private final Camera camera;
    private final Vector3f moveTarget;

    public FreeCameraController(Camera camera) {
        this.camera = camera;
        moveTarget = new Vector3f(camera.getPosition());
        rotation = new Vector3f(camera.getRotation());

        for (Map.Entry<MoveDirection, Integer> entry : moveKeyMap.entrySet()) {
            MessageBus.register(KeyEvent.released(entry.getValue()), () -> {
            });

            MessageBus.register(KeyEvent.keyHeldDown(entry.getValue()), () -> {
                move(entry.getKey());
            });
        }

        MessageBus.register(MouseEvent.MOVE, (event) -> {
            //noinspection SuspiciousNameCombination
            rotation.y += ((MouseEvent) event).getMouseMoveOffsetVector().x; // yaw
            //noinspection SuspiciousNameCombination
            rotation.x += ((MouseEvent) event).getMouseMoveOffsetVector().y; // pitch

            constraintPitch();
            camera.setRotation(rotation);
        });

        MessageBus.register(LifeCycle.UPDATE.eventID(), () -> {
            // TODO don't move if we are there
            camera.getPosition().lerp(moveTarget, Time.deltaTime * ACCELERATION);
            camera.forceUpdate();
        });
    }

    private void move(MoveDirection direction) {
        switch (direction) {
            case FORWARD:
                moveTarget.set(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z + MOVE_SPEED);
                break;
            case BACKWARD:
                moveTarget.set(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z - MOVE_SPEED);
                break;
            case LEFT:
                moveTarget.set(camera.getPosition().x - MOVE_SPEED, camera.getPosition().y, camera.getPosition().z);
                break;
            case RIGHT:
                moveTarget.set(camera.getPosition().x + MOVE_SPEED, camera.getPosition().y, camera.getPosition().z);
                break;
            case UPWARD:
                moveTarget.set(camera.getPosition().x, camera.getPosition().y + SCROLL_SPEED, camera.getPosition().z);
                break;
            case DOWNWARD:
                moveTarget.set(camera.getPosition().x, camera.getPosition().y - SCROLL_SPEED, camera.getPosition().z);
                break;
        }
    }

    private void constraintPitch() {
        if (rotation.x > 89) {
            rotation.x = 89;
        }
        if (rotation.x < -89) {
            rotation.x = -89;
        }
    }
}
