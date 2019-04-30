package spck.engine.ecs.ui;

import org.joml.Vector2f;
import spck.engine.Engine;

import java.util.Optional;

public class UICanvasScaler {
    public enum Type {CONSTANT_PIXEL, SCALE_WITH_SCREEN;}

    private final Type type;

    private Vector2f referenceResolution;
    private Float matchWidthOrHeightPercent;
    private UICanvasScaler(Type type) {
        this.type = type;
    }

    public static UICanvasScaler constantPixel() {
        return new UICanvasScaler(Type.CONSTANT_PIXEL);
    }

    /**
     * The Canvas Scaler component is used for controlling the overall scale and pixel density of UI elements in the Canvas.
     *
     * @param referenceResolution       The resolution the UI layout is designed for. If the screen resolution is larger, the UI will be scaled up, and if it’s smaller, the UI will be scaled down.
     * @param matchWidthOrHeightPercent Scale the canvas area with the width as reference, the height as reference, or something in between.
     * @return a scaler object that will be used during UI rendering
     */
    public static UICanvasScaler scaleWithPixel(Vector2f referenceResolution, float matchWidthOrHeightPercent) {
        if (matchWidthOrHeightPercent < 0 || matchWidthOrHeightPercent > 1) {
            throw new RuntimeException("Value must between 0 and 1");
        }

        UICanvasScaler scaler = new UICanvasScaler(Type.SCALE_WITH_SCREEN);
        scaler.referenceResolution = referenceResolution;
        scaler.matchWidthOrHeightPercent = matchWidthOrHeightPercent;
        return scaler;
    }

    public float apply(float size) {
        float widthRatio = (float) Engine.window.getPreferences().getWidth() / referenceResolution.x * matchWidthOrHeightPercent;
        float heightRatio = (float) Engine.window.getPreferences().getHeight() / referenceResolution.y * (1f - matchWidthOrHeightPercent);

        return (widthRatio + heightRatio) * size;
    }

    public Type getType() {
        return type;
    }

    public Optional<Vector2f> getReferenceResolution() {
        return Optional.ofNullable(referenceResolution);
    }

    public Optional<Float> getMatchWidthOrHeightPercent() {
        return Optional.ofNullable(matchWidthOrHeightPercent);
    }
}
