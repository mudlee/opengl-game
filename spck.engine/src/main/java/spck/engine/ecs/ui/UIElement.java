package spck.engine.ecs.ui;

import org.joml.Vector2f;
import org.joml.Vector2i;
import spck.engine.Engine;
import spck.engine.ui.UIObjectPosition;

public abstract class UIElement {
    protected UIObjectPosition position = UIObjectPosition.topLeft(0, 0);
    protected Vector2i screenOffset = new Vector2i().zero();
    protected Vector2f screenCoords = new Vector2f();
    protected int screenScaleFactor = 1;
    protected String id;

    UIElement() {
    }

    public String getId() {
        return id;
    }

    public UIObjectPosition getPosition() {
        return position;
    }

    public Vector2f getScreenCoords() {
        return screenCoords;
    }

    public void setScreenScaleFactor(int screenScaleFactor) {
        this.screenScaleFactor = screenScaleFactor;
        updateScreenCoords();
    }

    public void updateScreenCoords() {
        switch (position.getAlign()) {
            case TOP_LEFT:
                screenCoords.x = position.getLeft();
                screenCoords.y = position.getTop();
                break;
            case TOP_RIGHT:
                screenCoords.x = Engine.window.getPreferences().getWidth() - (position.getRight()) + screenOffset.x;
                screenCoords.y = position.getTop();
                break;
            case BOTTOM_LEFT:
                screenCoords.x = position.getLeft();
                screenCoords.y = Engine.window.getPreferences().getHeight()
                        - screenOffset.y
                        - position.getBottom();
                break;
            case BOTTOM_RIGHT:
                screenCoords.x = Engine.window.getPreferences().getWidth() - (position.getRight()) + screenOffset.x;
                screenCoords.y = Engine.window.getPreferences().getHeight()
                        - screenOffset.y
                        - position.getBottom();
                break;
            case CENTER_CENTER:
                screenCoords.x = (float) Engine.window.getPreferences().getWidth() / 2 - (float) screenOffset.x / 2;
                screenCoords.y = (float) Engine.window.getPreferences().getHeight() / 2 - (float) screenOffset.y / 2;
                break;
        }
    }
}
