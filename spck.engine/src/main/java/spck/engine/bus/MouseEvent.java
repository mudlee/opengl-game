package spck.engine.bus;

import org.joml.Vector2d;
import spck.engine.Engine;

public class MouseEvent {
    private static final float SENSITIVITY = 0.05f;
    private final Vector2d mousePosition = new Vector2d().zero();
    private final Vector2d mouseOffsetVector = new Vector2d().zero();
    private final Vector2d previousMousePosition = new Vector2d().zero();
    private boolean mouseFirstMove = true;

    public static String moved() {
        return Engine.ID + "MOUSE_MOVED";
    }

    public void calculate(double x, double y) {
        mousePosition.set(x, y);

        if (mouseFirstMove) {
            previousMousePosition.set(x, y);
            mouseFirstMove = false;
        }

        mouseOffsetVector.x = x - previousMousePosition.x;
        mouseOffsetVector.y = previousMousePosition.y - y; // Reversed since y-coordinates range from bottom to top

        previousMousePosition.set(x, y);

        mouseOffsetVector.mul(SENSITIVITY);
    }

    public Vector2d getMousePosition() {
        return mousePosition;
    }

    public Vector2d getMouseOffsetVector() {
        return mouseOffsetVector;
    }
}
