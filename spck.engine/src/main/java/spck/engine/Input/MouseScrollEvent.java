package spck.engine.Input;

import org.joml.Vector2d;

public class MouseScrollEvent {
    public final Vector2d offset = new Vector2d().zero();

    void calculateScroll(double xOffset, double yOffset) {
        offset.set(xOffset, yOffset);
    }
}
