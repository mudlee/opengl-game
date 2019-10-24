package spck.engine.ui;

import org.joml.Vector2i;
import spck.engine.Align;

public class UIObjectPosition {
    private final Vector2i position = new Vector2i();
    private Align align;

    private UIObjectPosition() {
    }

    public static UIObjectPosition build(int x, int y, Align align) {
        UIObjectPosition position = new UIObjectPosition();
        position.align = align;
        position.position.set(x, y);
        return position;
    }

    public void set(int x, int y) {
        position.set(x, y);
    }

    public Vector2i get() {
        return position;
    }

    public Align getAlign() {
        return align;
    }

    public void setAlign(Align align) {
        this.align = align;
    }
}
