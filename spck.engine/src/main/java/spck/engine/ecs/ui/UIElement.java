package spck.engine.ecs.ui;

import org.joml.Vector2f;
import org.joml.Vector2i;
import spck.engine.Align;
import spck.engine.Engine;
import spck.engine.ui.UIObjectPosition;
import spck.engine.window.GLFWWindow;

public abstract class UIElement {
    private final GLFWWindow window;
    protected UIObjectPosition position = UIObjectPosition.build(0, 0, Align.TOP_LEFT);
    protected Vector2i screenOffset = new Vector2i().zero();
    protected Vector2f screenCoords = new Vector2f();
    protected String id;

    UIElement(GLFWWindow window) {
        this.window = window;
    }

    public String getId() {
        return id;
    }

    public UIObjectPosition getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        updateScreenCoords();
    }

    public Vector2f getScreenCoords() {
        return screenCoords;
    }

    public void updateScreenCoords() {
        switch (position.getAlign()) {
            case TOP_LEFT:
                screenCoords.x = position.get().x;
                screenCoords.y = position.get().y;
                break;
            case TOP_RIGHT:
                screenCoords.x = window.getWidth() - (position.get().x) + screenOffset.x;
                screenCoords.y = position.get().y;
                break;
            case BOTTOM_LEFT:
                screenCoords.x = position.get().x;
                screenCoords.y = window.getHeight()
                        - screenOffset.y
                        - position.get().y;
                break;
            case BOTTOM_RIGHT:
                screenCoords.x = window.getWidth() - (position.get().y) + screenOffset.x;
                screenCoords.y = window.getHeight()
                        - screenOffset.y
                        - position.get().y;
                break;
            case CENTER_CENTER:
                screenCoords.x = (float) window.getWidth() / 2 - (float) screenOffset.x / 2;
                screenCoords.y = (float) window.getHeight() / 2 - (float) screenOffset.y / 2;
                break;
        }
    }
}
