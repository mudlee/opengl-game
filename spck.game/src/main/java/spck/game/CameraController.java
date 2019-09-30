package spck.game;

import org.joml.Vector3f;
import spck.engine.Axis;
import spck.engine.MoveDirection;
import spck.engine.Time;
import spck.engine.bus.KeyEvent;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.render.camera.Camera;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class CameraController {
    private static final Map<MoveDirection, Integer> moveKeyMap = new HashMap<>();
    private static final float MAX_SPEED = 0.5f;
    private static final float ACCELERATION = 0.2f;

    static {
        moveKeyMap.put(MoveDirection.LEFT, GLFW_KEY_A);
        moveKeyMap.put(MoveDirection.RIGHT, GLFW_KEY_D);
        moveKeyMap.put(MoveDirection.FORWARD, GLFW_KEY_W);
        moveKeyMap.put(MoveDirection.BACKWARD, GLFW_KEY_S);
        moveKeyMap.put(MoveDirection.UPWARD, GLFW_KEY_SPACE);
        moveKeyMap.put(MoveDirection.DOWNWARD, GLFW_KEY_LEFT_CONTROL);
    }

    private final Vector3f rotation;
    private final Vector3f movementVector = new Vector3f().zero();
    private final Map<Axis, Boolean> movingAxes = new HashMap<>();

    public CameraController(Camera camera) {
        movingAxes.put(Axis.X, false);
        movingAxes.put(Axis.Y, false);
        movingAxes.put(Axis.Z, false);

        rotation = new Vector3f(camera.getRotation());

        for (Map.Entry<MoveDirection, Integer> entry : moveKeyMap.entrySet()) {
            MessageBus.register(KeyEvent.released(entry.getValue()), () -> {
                moveStopped(entry.getKey());
            });

            MessageBus.register(KeyEvent.keyHeldDown(entry.getValue()), () -> {
                moving(entry.getKey());
            });
        }

        MessageBus.register(LifeCycle.UPDATE.eventID(), () -> {
            if (movementVector.x != 0 || movementVector.y != 0 || movementVector.z != 0) {
                camera.move(calculatePositionChange());
            }
        });
    }

    private Vector3f calculatePositionChange() {
        if (movementVector.x > 0) {
            if (!movingAxes.get(Axis.X)) {
                movementVector.x -= ACCELERATION * Time.deltaTime;
                if (movementVector.x < 0) {
                    movementVector.x = 0;
                }
            }
        } else if (movementVector.x < 0) {
            if (!movingAxes.get(Axis.X)) {
                movementVector.x += ACCELERATION * Time.deltaTime;
                if (movementVector.x > 0) {
                    movementVector.x = 0;
                }
            }
        }

        if (movementVector.z > 0) {
            if (!movingAxes.get(Axis.Z)) {
                movementVector.z -= ACCELERATION * Time.deltaTime;
                if (movementVector.z < 0) {
                    movementVector.z = 0;
                }
            }
        } else if (movementVector.z < 0) {
            if (!movingAxes.get(Axis.Z)) {
                movementVector.z += ACCELERATION * Time.deltaTime;
                if (movementVector.z > 0) {
                    movementVector.z = 0;
                }
            }
        }

        if (movementVector.y > 0) {
            if (!movingAxes.get(Axis.Y)) {
                movementVector.y -= ACCELERATION * Time.deltaTime;
                if (movementVector.y < 0) {
                    movementVector.y = 0;
                }
            }
        } else if (movementVector.y < 0) {
            if (!movingAxes.get(Axis.Y)) {
                movementVector.y += ACCELERATION * Time.deltaTime;
                if (movementVector.y > 0) {
                    movementVector.y = 0;
                }
            }
        }

        return movementVector;
    }

    private void moving(MoveDirection direction) {
        movingAxes.put(direction.getAxis(), true);

        switch (direction) {
            case LEFT:
                if (movementVector.x < -MAX_SPEED) {
                    break;
                }

                movementVector.x -= ACCELERATION * Time.deltaTime;
                break;
            case RIGHT:
                if (movementVector.x > MAX_SPEED) {
                    break;
                }
                movementVector.x += ACCELERATION * Time.deltaTime;
                break;
            case FORWARD:
                if (movementVector.z > MAX_SPEED) {
                    break;
                }
                movementVector.z += ACCELERATION * Time.deltaTime;
                break;
            case BACKWARD:
                if (movementVector.z < -MAX_SPEED) {
                    break;
                }
                movementVector.z -= ACCELERATION * Time.deltaTime;
                break;
            case UPWARD:
                if (movementVector.y > MAX_SPEED) {
                    break;
                }
                movementVector.y += ACCELERATION * Time.deltaTime;
                break;
            case DOWNWARD:
                if (movementVector.y < -MAX_SPEED) {
                    break;
                }
                movementVector.y -= ACCELERATION * Time.deltaTime;
                break;
        }
    }

    private void moveStopped(MoveDirection direction) {
        movingAxes.put(direction.getAxis(), false);
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
