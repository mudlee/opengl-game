package spck.engine.ecs.ui;

import org.joml.Vector2f;
import org.joml.Vector2i;
import spck.engine.Align;
import spck.engine.Engine;
import spck.engine.ui.UIObjectPosition;

public abstract class UIElement {
    protected UIObjectPosition position = UIObjectPosition.build(0, 0, Align.TOP_LEFT);
    protected Vector2i screenOffset = new Vector2i().zero();
    protected Vector2f screenCoords = new Vector2f();
    protected String id;

    UIElement() {
    }

    public String getId() {
        return id;
    }

    public UIObjectPosition getPosition() {
        return position;
    }

    public void setPosition(int x, int y) {
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
                screenCoords.x = Engine.window.getPreferences().getWidth() - (position.get().x) + screenOffset.x;
                screenCoords.y = position.get().y;
                break;
            case BOTTOM_LEFT:
                screenCoords.x = position.get().x;
                screenCoords.y = Engine.window.getPreferences().getHeight()
                        - screenOffset.y
                        - position.get().y;
                break;
            case BOTTOM_RIGHT:
                screenCoords.x = Engine.window.getPreferences().getWidth() - (position.get().y) + screenOffset.x;
                screenCoords.y = Engine.window.getPreferences().getHeight()
                        - screenOffset.y
                        - position.get().y;
                break;
            case CENTER_CENTER:
                screenCoords.x = (float) Engine.window.getPreferences().getWidth() / 2 - (float) screenOffset.x / 2;
                screenCoords.y = (float) Engine.window.getPreferences().getHeight() / 2 - (float) screenOffset.y / 2;
                break;
        }

        screenCoords.x *= Engine.window.getPreferences().getScreenScaleFactor().orElseThrow();
        screenCoords.y *= Engine.window.getPreferences().getScreenScaleFactor().orElseThrow();
    }
}
