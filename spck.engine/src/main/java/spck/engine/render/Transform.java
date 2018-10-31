package spck.engine.render;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import spck.engine.util.TransformationMatrixCreator;

public class Transform {
    private final Matrix4f transformationMatrixReusable = new Matrix4f();
    private Vector3f position = new Vector3f().zero();
    private Vector3f rotation = new Vector3f().zero();
    private Vector3f scale = new Vector3f(1, 1, 1);
    private boolean changed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transform)) return false;

        Transform transform = (Transform) o;

        if (!position.equals(transform.position)) return false;
        if (!rotation.equals(transform.rotation)) return false;
        return scale.equals(transform.scale);
    }

    @Override
    public int hashCode() {
        int result = position.hashCode();
        result = 31 * result + rotation.hashCode();
        result = 31 * result + scale.hashCode();
        return result;
    }

    public void ackAndComputeChanged(Runnable runnable) {
        if (changed) {
            changed = false;
            runnable.run();
        }
    }

    public Vector3f getPosition() {
        return position;
    }

    public Transform setPosition(Vector3f position) {
        this.position.set(position);
        dataChanged();
        return this;
    }

    public Transform setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        dataChanged();
        return this;
    }

    public Transform setPosition(Vector2f position) {
        this.position.x = position.x;
        this.position.y = position.y;
        dataChanged();
        return this;
    }

    public Transform setPosition(float x, float y) {
        this.position.x = x;
        this.position.y = y;
        dataChanged();
        return this;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Transform setRotation(Vector3f rotation) {
        this.rotation.set(rotation);
        dataChanged();
        return this;
    }

    public Vector3f getScale() {
        return scale;
    }

    public Transform setScale(Vector3f scale) {
        this.scale.set(scale);
        dataChanged();
        return this;
    }

    public Matrix4f getTransformationMatrix() {
        return transformationMatrixReusable;
    }

    private void dataChanged() {
        transformationMatrixReusable.set(TransformationMatrixCreator.create(position, rotation, scale));
        changed = true;
    }
}
