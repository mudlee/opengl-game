package spck.engine.bus;

import org.joml.Vector2d;

public class MouseEvent {
    public static final String MOVE = "MOUSE_MOVED";
    public static final String SCROLL = "MOUSE_SCROLLED";

    // MOVEMENT
    private static final float MOVE_SENSITIVITY = 0.05f;
    private final Vector2d mousePosition = new Vector2d().zero();
    private final Vector2d mouseMoveOffsetVector = new Vector2d().zero();
    private final Vector2d previousMousePosition = new Vector2d().zero();
    private boolean mouseFirstMove = true;

    // SCROLL
    private final Vector2d mouseScrollOffsetVector = new Vector2d().zero();

    public void calculateMovement(double xOffset, double yOffset) {
        mousePosition.set(xOffset, yOffset);

        if (mouseFirstMove) {
            previousMousePosition.set(xOffset, yOffset);
            mouseFirstMove = false;
        }

        mouseMoveOffsetVector.x = xOffset - previousMousePosition.x;
        mouseMoveOffsetVector.y = previousMousePosition.y - yOffset; // Reversed since y-coordinates range from bottom to top

        previousMousePosition.set(xOffset, yOffset);

        mouseMoveOffsetVector.mul(MOVE_SENSITIVITY);
    }

    public void calculateScroll(double xOffset, double yOffset) {
        mouseScrollOffsetVector.set(xOffset, yOffset);
    }

    public Vector2d getMousePosition() {
        return mousePosition;
    }

    public Vector2d getMouseMoveOffsetVector() {
        return mouseMoveOffsetVector;
    }

    public Vector2d getMouseScrollOffsetVector() {
        return mouseScrollOffsetVector;
    }
}
