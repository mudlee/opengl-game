package spck.engine.ecs.ui;

import spck.engine.Engine;
import spck.engine.framework.RGBAColor;
import spck.engine.ui.UIObjectPosition;

public class UIText extends UIElement {
    private String text;
    private float size;
    private RGBAColor color;
    private String font;
    private int align;

    public UIText(String text, float size, UIObjectPosition position, RGBAColor color, String font, int align, String customID) {
        this.customID = customID;
        this.text = text;
        this.size = size;
        this.color = color;
        this.font = font;
        this.align = align;
        this.position = position;
        this.screenOffset.set(0, (int) size);
        updateScreenCoords(Engine.window.getPreferences().getScreenScaleFactor());
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setColor(RGBAColor color) {
        this.color = color;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    public String getText() {
        return text;
    }

    public float getSize() {
        return size;
    }

    public RGBAColor getColor() {
        return color;
    }

    public String getFont() {
        return font;
    }

    public int getAlign() {
        return align;
    }
}
