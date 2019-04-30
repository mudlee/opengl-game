package spck.engine.ecs.ui;

import org.joml.Vector2f;
import org.joml.Vector2i;
import spck.engine.Engine;
import spck.engine.ui.UIObjectPosition;
import spck.engine.util.Pixel;

class UIElement {
    protected String customID;
    protected UIObjectPosition position = UIObjectPosition.topLeft(0, 0);
    protected Vector2i screenOffset = new Vector2i().zero();
    protected Vector2f screenCoords = new Vector2f();
    protected int screenScaleFactor = 1;

    void updateScreenCoords(int screenScaleFactor) {
        this.screenScaleFactor = screenScaleFactor;

        switch (position.getAlign()) {
            case TOP_LEFT:
                screenCoords.x = Pixel.scaled(position.getLeft());
                screenCoords.y = Pixel.scaled(position.getTop());
                break;
            case TOP_RIGHT:
                screenCoords.x = Engine.window.getPreferences().getWidth() - (Pixel.scaled(position.getRight()) + Pixel.scaled(screenOffset.x));
                screenCoords.y = Pixel.scaled(position.getTop());
                break;
            case BOTTOM_LEFT:
                screenCoords.x = Pixel.scaled(position.getLeft());
                screenCoords.y = Engine.window.getPreferences().getHeight()
                        - Pixel.scaled(screenOffset.y)
                        - Pixel.scaled(position.getBottom());
                break;
            case BOTTOM_RIGHT:
                screenCoords.x = Engine.window.getPreferences().getWidth() - (Pixel.scaled(position.getRight()) + Pixel.scaled(screenOffset.x));
                screenCoords.y = Engine.window.getPreferences().getHeight()
                        - Pixel.scaled(screenOffset.y)
                        - Pixel.scaled(position.getBottom());
                break;
            case CENTER_CENTER:
                screenCoords.x = (float) Engine.window.getPreferences().getWidth() / 2 - (float) Pixel.scaled(screenOffset.x) / 2;
                screenCoords.y = (float) Engine.window.getPreferences().getHeight() / 2 - (float) Pixel.scaled(screenOffset.y) / 2;
                break;
        }
    }

    public void setCustomID(String customID) {
        this.customID = customID;
    }

    public void setPosition(UIObjectPosition position) {
        this.position = position;
    }

    public void setScreenOffset(Vector2i screenOffset) {
        this.screenOffset = screenOffset;
    }

    public void setScreenCoords(Vector2f screenCoords) {
        this.screenCoords = screenCoords;
    }

    public void setScreenScaleFactor(int screenScaleFactor) {
        this.screenScaleFactor = screenScaleFactor;
    }

    public String getCustomID() {
        return customID;
    }

    public UIObjectPosition getPosition() {
        return position;
    }

    public Vector2i getScreenOffset() {
        return screenOffset;
    }

    public Vector2f getScreenCoords() {
        return screenCoords;
    }

    public int getScreenScaleFactor() {
        return screenScaleFactor;
    }
}
