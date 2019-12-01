package spck.engine.ui;

import org.joml.Vector2f;
import spck.engine.Align;

public class UIObjectPosition {
    private final Vector2f position = new Vector2f();
    private Align align;

    private UIObjectPosition() {
    }

    public static UIObjectPosition build(int x, int y, Align align) {
        UIObjectPosition position = new UIObjectPosition();
        position.align = align;
        position.position.set(x, y);
        return position;
    }

    public void set(float x, float y) {
        position.set(x, y);
    }

    public Vector2f get() {
        return position;
    }

    public Align getAlign() {
        return align;
    }

    public void setAlign(Align align) {
        this.align = align;
    }
}
