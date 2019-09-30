package spck.engine.render.camera;

import org.joml.Matrix4f;
import org.joml.Rayf;
import org.joml.Vector3f;

public interface Camera {
    void setRotation(Vector3f rotation);

    void setPosition(Vector3f position);

    void forceUpdate();

    Rayf getRay();

    void move(Vector3f moveVector);

    Vector3f getPosition();

    Vector3f getRotation();

    Matrix4f getViewMatrix();

    Matrix4f getProjectionMatrix();

    boolean isViewMatrixChanged();

    boolean isProjectionMatrixChanged();

    boolean isPositionChanged();


}
