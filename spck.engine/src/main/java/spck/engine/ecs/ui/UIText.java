package spck.engine.ecs.ui;

import spck.engine.Engine;
import spck.engine.framework.RGBAColor;
import spck.engine.framework.UIRenderer;
import spck.engine.ui.UIObjectPosition;

public class UIText extends UIElement {
    private String text;
    private int size = 15;
    private RGBAColor color = RGBAColor.white();
    private String font = Engine.preferences.defaultFont;
    private int align = UIRenderer.Align.LEFT.getValue() | UIRenderer.Align.TOP.getValue();

    private UIText() {
    }

    public static UIText build(String text, UIObjectPosition position) {
        UIText uiText = new UIText();
        uiText.text = text;
        uiText.position = position;
        uiText.screenOffset.set(0, (int) uiText.size);
        uiText.size *= Engine.window.getPreferences().getDevicePixelRatio().orElseThrow();
        uiText.updateScreenCoords();
        return uiText;
    }

    public UIText size(int size) {
        this.size = size * Engine.window.getPreferences().getDevicePixelRatio().orElseThrow();
        return this;
    }

    public UIText font(String font) {
        this.font = font;
        return this;
    }

    public UIText color(RGBAColor color) {
        this.color = color;
        return this;
    }

    public UIText align(int align) {
        this.align = align;
        return this;
    }

    public UIText id(String id) {
        this.id = id;
        return this;
    }

    public UIText text(String text) {
        this.text = text;
        return this;
    }

    public String getText() {
        return text;
    }

    public int getSize() {
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
