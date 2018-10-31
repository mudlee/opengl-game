package spck.engine.ecs.ui;

import spck.engine.Engine;
import spck.engine.framework.RGBAColor;
import spck.engine.framework.UIRenderer;

public class UITextComponent extends UIComponent {
    public String text;
    public float size = 15;
    public RGBAColor color = RGBAColor.white();
    public String font = Engine.preferences.defaultFont;
    public int align = UIRenderer.Align.LEFT.getValue() | UIRenderer.Align.TOP.getValue();
}
