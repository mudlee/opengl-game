package spck.engine.ui.ecs;

import org.lwjgl.nanovg.NVGColor;
import spck.engine.Engine;
import spck.engine.util.RGBAColor;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;

public class UITextComponent extends AbstractUIComponent {
    public String text;
    public float size = 15;
    public NVGColor color = RGBAColor.nvgBlack();
    public String font = Engine.preferences.defaultFont;
    public int align = NVG_ALIGN_LEFT | NVG_ALIGN_TOP;
}
