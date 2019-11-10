package spck.engine.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TransformationMatrixCreator {
    private static Matrix4f reusable = new Matrix4f();

    private TransformationMatrixCreator() {
    }

    public static Matrix4f create(Vector3f translation, Vector3f rotation, Vector3f scale) {
        reusable.identity();
        reusable.
                translate(translation).
                rotateX((float) Math.toRadians(rotation.x)).
                rotateY((float) Math.toRadians(rotation.y)).
                rotateZ((float) Math.toRadians(rotation.z)).
                scale(scale);
        return reusable;
    }

    public static Matrix4f create(Vector3f translation) {
        reusable.identity();
        reusable.translate(translation);
        return reusable;
    }
}
