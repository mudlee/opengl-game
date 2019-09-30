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
    public void move(Vector3f moveVector) {
        // use this
        REUSABLE_FRONT_VECTOR.x = (float) (Math.cos(0) * Math.cos(Math.toRadians(rotation.y)));
        REUSABLE_FRONT_VECTOR.y = (float) (Math.sin(0));
        REUSABLE_FRONT_VECTOR.z = (float) (Math.cos(0) * Math.sin(Math.toRadians(rotation.y)));
        REUSABLE_FRONT_VECTOR.normalize();

        if (moveVector.x != 0) {
            REUSABLE_3D_VECTOR.set(REUSABLE_FRONT_VECTOR);
            position.add(REUSABLE_3D_VECTOR.cross(REUSABLE_UP_VECTOR).normalize().mul(moveVector.x));
            positionChanged = true;
        }

        if (moveVector.z != 0) {
            REUSABLE_3D_VECTOR.set(REUSABLE_FRONT_VECTOR);
            position.add(REUSABLE_3D_VECTOR.mul(moveVector.z));
            positionChanged = true;
        }

        if (moveVector.y != 0) {
            position.y += moveVector.y;
            positionChanged = true;
        }

        if (positionChanged) {
            updateViewMatrix();
        }
    }
}
