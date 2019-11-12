package spck.engine.Input;

import org.joml.Vector2d;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;
import spck.engine.Engine;
import spck.engine.MoveDirection;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;

import java.nio.DoubleBuffer;
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
    // MOUSE CURSOR POSITION
    private static final double MOUSE_SENSITIVITY = 30f;
    private static DoubleBuffer mouseCursorAbsolutePositionX = MemoryUtil.memAllocDouble(1);
    private static DoubleBuffer mouseCursorAbsolutePositionY = MemoryUtil.memAllocDouble(1);
    private static final Vector2d MOUSE_CURSOR_POSITION_REUSABLE = new Vector2d();
    private static final Vector2d mouseCursorRelativePosition = new Vector2d();
    private static boolean mouseCursorRelativePositionInitalized = false;

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
        MessageBus.register(LifeCycle.CLEANUP.eventID(), () -> {
            MemoryUtil.memFree(mouseCursorAbsolutePositionX);
            MemoryUtil.memFree(mouseCursorAbsolutePositionY);
        });
    }

    public static void setMousePosition(Vector2d position) {
        glfwSetCursorPos(Engine.window.getID(), position.x, position.y);
    }

    /**
     * Returns the mouse's relative position which means if the application is running in windowed mode or
     * the cursor is hidden, then the mouse position cannot be less than 0 or greater than the window's width/height.
     */
    public static Vector2d getMouseRelativePosition() {
        if (!mouseCursorRelativePositionInitalized) {
            Vector2d mouseAbsPos = getMouseAbsolutePosition();
            calculateMovement(mouseAbsPos.x, mouseAbsPos.y);
            mouseCursorRelativePositionInitalized = true;
        }
        return mouseCursorRelativePosition;
    }

    /**
     * Returns the mouse's absolute position which means if the application is running in windowed mode or
     * the cursor is hidden, then if the mouse is outside the window, the values can be negative or greater than the
     * window's width/height.
     */
    public static Vector2d getMouseAbsolutePosition() {
        mouseCursorAbsolutePositionX.clear();
        mouseCursorAbsolutePositionY.clear();
        glfwGetCursorPos(Engine.window.getID(), mouseCursorAbsolutePositionX, mouseCursorAbsolutePositionY);
        MOUSE_CURSOR_POSITION_REUSABLE.set(
                mouseCursorAbsolutePositionX.get(),
                mouseCursorAbsolutePositionY.get()
        );
        return MOUSE_CURSOR_POSITION_REUSABLE;
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

    private static void calculateMovement(double x, double y) {
        mouseMoveEvent.position.set(x, y);

        if (mouseFirstMove) {
            previousMousePosition.set(x, y);
            mouseFirstMove = false;
        }

        mouseMoveEvent.offset.set(
                x - previousMousePosition.x,
                previousMousePosition.y - y // Reversed since y-coordinates range from bottom to top
        );

        previousMousePosition.set(x, y);
        mouseMoveEvent.offset.mul(MOVE_SENSITIVITY);

        double newX = mouseCursorRelativePosition.x + mouseMoveEvent.offset.x * MOUSE_SENSITIVITY;
        double newY = mouseCursorRelativePosition.y - mouseMoveEvent.offset.y * MOUSE_SENSITIVITY;

        boolean xMoved = false;
        boolean yMoved = false;

        if (newX < 0) {
            newX = 0;
            mouseMoveEvent.direction = MoveDirection.LEFT;
            xMoved = true;
        } else if (newX > Engine.window.getPreferences().getWidth()) {
            newX = Engine.window.getPreferences().getWidth();
            mouseMoveEvent.direction = MoveDirection.RIGHT;
            xMoved = true;
        }

        if (newY < 0) {
            newY = 0;
            mouseMoveEvent.direction = MoveDirection.FORWARD;
            yMoved = true;
        } else if (newY > Engine.window.getPreferences().getHeight()) {
            newY = Engine.window.getPreferences().getHeight();
            mouseMoveEvent.direction = MoveDirection.BACKWARD;
            yMoved = true;
        }

        if (!xMoved && !yMoved) {
            mouseMoveEvent.direction = MoveDirection.STILL;
        }

        mouseCursorRelativePosition.set(newX, newY);
        mouseMoveEvent.relativePosition.set(newX, newY);
    }
}
