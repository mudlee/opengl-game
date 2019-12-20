package spck.engine.window;

import org.joml.Vector2d;
import spck.engine.MoveDirection;
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
	private final List<Consumer<MouseMoveEvent>> mouseMoveHandlers = new ArrayList<>();
	private final float MOVE_SENSITIVITY = 0.05f;
	private final Vector2d previousMousePosition = new Vector2d().zero();
	private boolean mouseFirstMove = true;
	private final MouseMoveEvent mouseMoveEvent = new MouseMoveEvent();
	// MOUSE SCROLL EVENTS
	private final List<Consumer<MouseScrollEvent>> mouseScrollHandlers = new ArrayList<>();
	private final MouseScrollEvent mouseScrollEvent = new MouseScrollEvent();
	// MOUSE BUTTON EVENTS
	private final Map<Integer, List<Consumer<MouseButtonEvent>>> mouseButtonHeldDownHandlers = new HashMap<>();
	private final Map<Integer, List<Consumer<MouseButtonEvent>>> mouseButtonPressedHandlers = new HashMap<>();
	private final Map<Integer, List<Consumer<MouseButtonEvent>>> mouseButtonReleasedHandlers = new HashMap<>();
	private final List<Integer> mouseButtonsDown = new ArrayList<>();
	private final MouseButtonEvent mouseButtonHeldDownEvent = new MouseButtonEvent();
	private final MouseButtonEvent mouseButtonPressedEvent = new MouseButtonEvent();
	private final MouseButtonEvent mouseButtonReleasedEvent = new MouseButtonEvent();
	// KEY EVENTS
	private final Map<Integer, List<Consumer<KeyEvent>>> keyHeldDownHandlers = new HashMap<>();
	private final Map<Integer, List<Consumer<KeyEvent>>> keyPressedHandlers = new HashMap<>();
	private final Map<Integer, List<Consumer<KeyEvent>>> keyReleasedHandlers = new HashMap<>();
	private final List<Integer> keyboardKeysDown = new ArrayList<>();
	private final KeyEvent keyHeldDownEvent = new KeyEvent();
	private final KeyEvent keyPressedEvent = new KeyEvent();
	private final KeyEvent keyReleasedEvent = new KeyEvent();
	// MOUSE CURSOR POSITION
	private final double MOUSE_SENSITIVITY = 30f;
	private final Vector2d MOUSE_CURSOR_POSITION_REUSABLE = new Vector2d();
	private final Vector2d mouseCursorRelativePosition = new Vector2d();
	private final Vector2d mouseCursorPositionSupply = new Vector2d();
	private boolean mouseCursorRelativePositionInitalized = false;

	// WINDOW
	private int windowWidth;
	private int windowHeight;
	private Consumer<Vector2d> cursorPosSupplier;

	public void create(int windowWidth, int windowHeight, Consumer<Vector2d> cursorPosSupplier) {
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
		this.cursorPosSupplier = cursorPosSupplier;
		MessageBus.register(LifeCycle.UPDATE.eventID(), this::onUpdate);
	}

	public void onMouseMove(Consumer<MouseMoveEvent> handler) {
		mouseMoveHandlers.add(handler);
	}

	public void onKeyHeldDown(int keyCode, Consumer<KeyEvent> handler) {
		keyHeldDownHandlers.putIfAbsent(keyCode, new ArrayList<>());
		keyHeldDownHandlers.get(keyCode).add(handler);
	}

	public void onKeyPressed(int keyCode, Consumer<KeyEvent> handler) {
		keyPressedHandlers.putIfAbsent(keyCode, new ArrayList<>());
		keyPressedHandlers.get(keyCode).add(handler);
	}

	public void onKeyReleased(int keyCode, Consumer<KeyEvent> handler) {
		keyReleasedHandlers.putIfAbsent(keyCode, new ArrayList<>());
		keyReleasedHandlers.get(keyCode).add(handler);
	}

	public void onMouseScroll(Consumer<MouseScrollEvent> handler) {
		mouseScrollHandlers.add(handler);
	}

	public void onMouseButtonHeldDown(int keyCode, Consumer<MouseButtonEvent> handler) {
		mouseButtonHeldDownHandlers.putIfAbsent(keyCode, new ArrayList<>());
		mouseButtonHeldDownHandlers.get(keyCode).add(handler);
	}

	public void onMouseButtonPressed(int keyCode, Consumer<MouseButtonEvent> handler) {
		mouseButtonPressedHandlers.putIfAbsent(keyCode, new ArrayList<>());
		mouseButtonPressedHandlers.get(keyCode).add(handler);
	}

	public void onMouseButtonReleased(int keyCode, Consumer<MouseButtonEvent> handler) {
		mouseButtonReleasedHandlers.putIfAbsent(keyCode, new ArrayList<>());
		mouseButtonReleasedHandlers.get(keyCode).add(handler);
	}

	/**
	 * Returns the mouse's relative position which means if the application is running in windowed mode or
	 * the cursor is hidden, then the mouse position cannot be less than 0 or greater than the window's width/height.
	 */
	public Vector2d getMouseRelativePosition() {
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
	public Vector2d getMouseAbsolutePosition() {
		cursorPosSupplier.accept(mouseCursorPositionSupply);
		MOUSE_CURSOR_POSITION_REUSABLE.set(mouseCursorPositionSupply.x, mouseCursorPositionSupply.y);
		return MOUSE_CURSOR_POSITION_REUSABLE;
	}

	void windowResizedCallback(int windowWidth, int windowHeight) {
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
	}

	void keyCallback(int key, int scancode, int action, int mods) {
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

	void mouseButtonCallback(int button, int action, int mods) {
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

	void cursorPosCallback(double x, double y) {
		calculateMovement(x, y);
		for (Consumer<MouseMoveEvent> handler : mouseMoveHandlers) {
			handler.accept(mouseMoveEvent);
		}
	}

	void mouseScrollCallback(double xOffset, double yOffset) {
		mouseScrollEvent.calculateScroll(xOffset, yOffset);
		for (Consumer<MouseScrollEvent> handler : mouseScrollHandlers) {
			handler.accept(mouseScrollEvent);
		}
	}

	private void onUpdate() {
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
	}

	private void calculateMovement(double x, double y) {
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
		} else if (newX > windowWidth) {
			newX = windowWidth;
			mouseMoveEvent.direction = MoveDirection.RIGHT;
			xMoved = true;
		}

		if (newY < 0) {
			newY = 0;
			mouseMoveEvent.direction = MoveDirection.UPWARD;
			yMoved = true;
		} else if (newY > windowHeight) {
			newY = windowHeight;
			mouseMoveEvent.direction = MoveDirection.DOWNWARD;
			yMoved = true;
		}

		if (!xMoved && !yMoved) {
			mouseMoveEvent.direction = MoveDirection.STILL;
		}

		mouseCursorRelativePosition.set(newX, newY);
		mouseMoveEvent.relativePosition.set(newX, newY);
	}
}
