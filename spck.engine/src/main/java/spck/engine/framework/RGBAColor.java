package spck.engine.framework;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;

public class RGBAColor {
    private NVGColor color;

    private RGBAColor(NVGColor color) {
        this.color = color;
    }

    NVGColor getColor() {
        return color;
    }

    public static NVGColor rgbaToNVGColor(int r, int g, int b, int a) {
        NVGColor color = NVGColor.create();
        color.r(r / 255.0f);
        color.g(g / 255.0f);
        color.b(b / 255.0f);
        color.a(a / 255.0f);
        return color;
    }

    public static Vector3f rgbToVector3f(int r, int g, int b) {
        return new Vector3f(r / 255.0f, g / 255.0f, b / 255.0f);
    }

    public static Vector4f rgbaToVector4f(int r, int g, int b, int a) {
        return new Vector4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
    }

    public static RGBAColor white() {
        NVGColor color = NVGColor.create();
        color.r(1);
        color.g(1);
        color.b(1);
        color.a(1);
        return new RGBAColor(color);
    }

    public static RGBAColor black() {
        NVGColor color = NVGColor.create();
        color.r(0);
        color.g(0);
        color.b(0);
        color.a(1);
        return new RGBAColor(color);
    }
}
