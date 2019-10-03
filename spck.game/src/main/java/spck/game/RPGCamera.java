package spck.game;

import org.joml.Math;
import org.joml.Vector3f;
import spck.engine.render.camera.Camera;
import spck.engine.render.camera.PerspectiveCamera;

public class RPGCamera extends PerspectiveCamera implements Camera {
    private final Vector3f REUSABLE_FRONT_VECTOR = new Vector3f(0, 0, -1);

    /**
     * Initialising a perspective projection.
     * <p>
     * Example: new PerspectiveCamera(60f, 0.01f, 1000f);
     *
     * @param fov
     * @param zNear
     * @param zFar
     * @return PerspectiveCamera
     */
    public RPGCamera(float fov, float zNear, float zFar) {
        super(fov, zNear, zFar);
    }

    @Override
    public void forceUpdate() {
        positionChanged = true;
        viewMatrixChanged = true;
        updateViewMatrix();
    }

    @Override
    public void setRotation(Vector3f rotation) {
        super.setRotation(rotation);
        REUSABLE_FRONT_VECTOR.x = (float) (Math.cos(Math.toRadians(rotation.y)));
        REUSABLE_FRONT_VECTOR.z = (float) (Math.sin(Math.toRadians(rotation.y)));
        REUSABLE_FRONT_VECTOR.normalize();
    }
}
