package spck.engine.ecs.ui;

import org.joml.Vector2f;

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

    public static UICanvasScaler scaleWithPixel(Vector2f referenceResolution, float matchWidthOrHeightPercent) {
        UICanvasScaler scaler = new UICanvasScaler(Type.SCALE_WITH_SCREEN);
        scaler.referenceResolution = referenceResolution;
        scaler.matchWidthOrHeightPercent = matchWidthOrHeightPercent;
        return scaler;
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
