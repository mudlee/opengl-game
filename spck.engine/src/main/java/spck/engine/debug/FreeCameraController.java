package spck.engine.debug;

import org.joml.Vector3f;
import spck.engine.Engine;
import spck.engine.MoveDirection;
import spck.engine.Time;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.render.camera.Camera;
import spck.engine.window.GLFWWindow;
import spck.engine.window.Input;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class FreeCameraController {
    private static final Map<MoveDirection, Integer> moveKeyMap = new HashMap<>();

    private static final float ACCELERATION = 3f;
    private static final float MOVE_SPEED = 1f;
    private static final Vector3f REUSABLE_VECTOR = new Vector3f();
    private final Vector3f REUSABLE_UP_VECTOR = new Vector3f(0, 1, 0);

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

    public FreeCameraController(Camera camera, GLFWWindow window, Input input) {
        this.camera = camera;
        moveTarget = new Vector3f(camera.getPosition());
        rotation = new Vector3f(camera.getRotation());
        window.captureMouse();

        for (Map.Entry<MoveDirection, Integer> entry : moveKeyMap.entrySet()) {
            input.onKeyHeldDown(entry.getValue(), event -> move(entry.getKey()));
        }

        input.onMouseMove(event -> {
            //noinspection SuspiciousNameCombination
            rotation.y += event.offset.x; // yaw
            //noinspection SuspiciousNameCombination
            rotation.x += event.offset.y; // pitch

            constraintPitch();
            camera.setRotation(rotation);
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
                moveTarget.set(camera.getPosition()).add(REUSABLE_VECTOR.mul(MOVE_SPEED));
                break;
            case BACKWARD:
                REUSABLE_VECTOR.set(camera.getFrontVector());
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
                moveTarget.set(camera.getPosition().x, camera.getPosition().y + MOVE_SPEED, camera.getPosition().z);
                break;
            case DOWNWARD:
                moveTarget.set(camera.getPosition().x, camera.getPosition().y - MOVE_SPEED, camera.getPosition().z);
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
