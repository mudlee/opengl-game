package spck.engine.render.camera;

import org.joml.Matrix4f;
import org.joml.Rayf;
import org.joml.Vector3f;

public interface Camera {
    void setRotation(Vector3f rotation);

    void setPosition(Vector3f position);

    void forceUpdate();

    Vector3f getFrontVector();

    Rayf getRay();

    Vector3f getPosition();

    Vector3f getRotation();

    Matrix4f getViewMatrix();

    Matrix4f getProjectionMatrix();

    boolean isViewMatrixChanged();

    boolean isProjectionMatrixChanged();

    boolean isPositionChanged();


}
