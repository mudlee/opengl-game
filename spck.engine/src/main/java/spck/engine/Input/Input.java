package spck.engine.Input;

import org.joml.Vector2d;
import org.lwjgl.glfw.*;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

public class Input {
    // MOUSE MOVEMENT EVENTS
    private static final List<Consumer<MouseMoveEvent>> mouseMoveHandlers = new ArrayList<>();
    private static final float MOVE_SENSITIVITY = 0.05f;
    private static final Vector2d previousMousePosition = new Vector2d().zero();
    private static boolean mouseFirstMove = true;
    private static final MouseMoveEvent mouseMoveEvent = new MouseMoveEvent();
    // MOUSE SCROLL EVENTS
    private static final List<Consumer<MouseScrollEvent>> mouseScrollHandlers = new ArrayList<>();
    private static final MouseScrollEvent mouseScrollEvent = new MouseScrollEvent();
    // MOUSE BUTTON EVENTS
    private static final Map<Integer, List<Consumer<MouseButtonEvent>>> mouseButtonHeldDownHandlers = new HashMap<>();
    private static final Map<Integer, List<Consumer<MouseButtonEvent>>> mouseButtonPressedHandlers = new HashMap<>();
    private static final Map<Integer, List<Consumer<MouseButtonEvent>>> mouseButtonReleasedHandlers = new HashMap<>();
    private static final List<Integer> mouseButtonsDown = new ArrayList<>();
    private static final MouseButtonEvent mouseButtonHeldDownEvent = new MouseButtonEvent();
    private static final MouseButtonEvent mouseButtonPressedEvent = new MouseButtonEvent();
    private static final MouseButtonEvent mouseButtonReleasedEvent = new MouseButtonEvent();
    // KEY EVENTS
    private static final Map<Integer, List<Consumer<KeyEvent>>> keyHeldDownHandlers = new HashMap<>();
    private static final Map<Integer, List<Consumer<KeyEvent>>> keyPressedHandlers = new HashMap<>();
    private static final Map<Integer, List<Consumer<KeyEvent>>> keyReleasedHandlers = new HashMap<>();
    private static final List<Integer> keyboardKeysDown = new ArrayList<>();
    private static final KeyEvent keyHeldDownEvent = new KeyEvent();
    private static final KeyEvent keyPressedEvent = new KeyEvent();
    private static final KeyEvent keyReleasedEvent = new KeyEvent();


    public static void initialize() {
        MessageBus.register(LifeCycle.UPDATE.eventID(), () -> {
            if (!keyboardKeysDown.isEmpty()) {
                for (Integer keyCode : keyboardKeysDown) {
                    if (keyHeldDownHandlers.containsKey(keyCode)) {
                        keyHeldDownEvent.set(keyCode, -1, GLFW_REPEAT, -1);
                        for (Consumer<KeyEvent> handler : keyHeldDownHandlers.get(keyCode)) {
                            handler.accept(keyHeldDownEvent);
                        }
                    }
                }
            }

            if (!mouseButtonsDown.isEmpty()) {
                for (Integer buttonCode : mouseButtonsDown) {
                    if (mouseButtonHeldDownHandlers.containsKey(buttonCode)) {
                        mouseButtonHeldDownEvent.set(buttonCode, GLFW_REPEAT, -1);
                        for (Consumer<MouseButtonEvent> handler : mouseButtonHeldDownHandlers.get(buttonCode)) {
                            handler.accept(mouseButtonHeldDownEvent);
                        }
                    }
                }
            }
        });
    }

    public static void onMouseMove(Consumer<MouseMoveEvent> handler) {
        mouseMoveHandlers.add(handler);
    }

    public static void onKeyHeldDown(int keyCode, Consumer<KeyEvent> handler) {
        keyHeldDownHandlers.putIfAbsent(keyCode, new ArrayList<>());
        keyHeldDownHandlers.get(keyCode).add(handler);
    }

    public static void onKeyPressed(int keyCode, Consumer<KeyEvent> handler) {
        keyPressedHandlers.putIfAbsent(keyCode, new ArrayList<>());
        keyPressedHandlers.get(keyCode).add(handler);
    }

    public static void onKeyReleased(int keyCode, Consumer<KeyEvent> handler) {
        keyReleasedHandlers.putIfAbsent(keyCode, new ArrayList<>());
        keyReleasedHandlers.get(keyCode).add(handler);
    }

    public static void onMouseScroll(Consumer<MouseScrollEvent> handler) {
        mouseScrollHandlers.add(handler);
    }

    public static void onMouseButtonHeldDown(int keyCode, Consumer<MouseButtonEvent> handler) {
        mouseButtonHeldDownHandlers.putIfAbsent(keyCode, new ArrayList<>());
        mouseButtonHeldDownHandlers.get(keyCode).add(handler);
    }

    public static void onMouseButtonPressed(int keyCode, Consumer<MouseButtonEvent> handler) {
        mouseButtonPressedHandlers.putIfAbsent(keyCode, new ArrayList<>());
        mouseButtonPressedHandlers.get(keyCode).add(handler);
    }

    public static void onMouseButtonReleased(int keyCode, Consumer<MouseButtonEvent> handler) {
        mouseButtonReleasedHandlers.putIfAbsent(keyCode, new ArrayList<>());
        mouseButtonReleasedHandlers.get(keyCode).add(handler);
    }

    public static GLFWCursorPosCallbackI mouseCursorCallback() {
        return new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                calculateMovement(x, y);
                for (Consumer<MouseMoveEvent> handler : mouseMoveHandlers) {
                    handler.accept(mouseMoveEvent);
                }
            }
        };
    }

    public static GLFWKeyCallbackI keyCallback() {
        return new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (GLFW_KEY_LAST + 1 < key || key < 0) {
                    return;
                }

                if (action == GLFW_PRESS) {
                    keyboardKeysDown.add(key);
                    if (keyPressedHandlers.containsKey(key)) {
                        keyPressedEvent.set(key, scancode, action, mods);
                        for (Consumer<KeyEvent> handler : keyPressedHandlers.get(key)) {
                            handler.accept(keyPressedEvent);
                        }
                    }
                } else if (action == GLFW_RELEASE) {
                    keyboardKeysDown.remove(Integer.valueOf(key));
                    if (keyReleasedHandlers.containsKey(key)) {
                        keyReleasedEvent.set(key, scancode, action, mods);
                        for (Consumer<KeyEvent> handler : keyReleasedHandlers.get(key)) {
                            handler.accept(keyReleasedEvent);
                        }
                    }
                }
            }
        };
    }

    public static GLFWScrollCallbackI mouseScrollCallback() {
        return new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xOffset, double yOffset) {
                mouseScrollEvent.calculateScroll(xOffset, yOffset);
                for (Consumer<MouseScrollEvent> handler : mouseScrollHandlers) {
                    handler.accept(mouseScrollEvent);
                }
            }
        };
    }

    public static GLFWMouseButtonCallbackI mouseButtonCallback() {
        return new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (action == GLFW_PRESS) {
                    mouseButtonsDown.add(button);
                    if (mouseButtonPressedHandlers.containsKey(button)) {
                        mouseButtonPressedEvent.set(button, action, mods);
                        for (Consumer<MouseButtonEvent> handler : mouseButtonPressedHandlers.get(button)) {
                            handler.accept(mouseButtonPressedEvent);
                        }
                    }
                } else if (action == GLFW_RELEASE) {
                    mouseButtonsDown.remove(Integer.valueOf(button));
                    if (mouseButtonReleasedHandlers.containsKey(button)) {
                        mouseButtonReleasedEvent.set(button, action, mods);
                        for (Consumer<MouseButtonEvent> handler : mouseButtonReleasedHandlers.get(button)) {
                            handler.accept(mouseButtonReleasedEvent);
                        }
                    }
                }
            }
        };
    }

    private static void calculateMovement(double xOffset, double yOffset) {
        mouseMoveEvent.position.set(xOffset, yOffset);

        if (mouseFirstMove) {
            previousMousePosition.set(xOffset, yOffset);
            mouseFirstMove = false;
        }

        mouseMoveEvent.offset.set(
                xOffset - previousMousePosition.x,
                previousMousePosition.y - yOffset // Reversed since y-coordinates range from bottom to top
        );

        previousMousePosition.set(xOffset, yOffset);
        mouseMoveEvent.offset.mul(MOVE_SENSITIVITY);
    }
}
