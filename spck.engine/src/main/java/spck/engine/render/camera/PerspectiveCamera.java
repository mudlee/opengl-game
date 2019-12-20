package spck.engine.render.camera;

import spck.engine.Engine;
import spck.engine.window.GLFWWindow;

public class PerspectiveCamera extends AbstractCamera implements Camera {
    /**
     * Initialising a perspective projection.
     * <p>
     * Example: new PerspectiveCamera(60f, 0.01f, 1000f);
     *
     * @param fov,   for default, use 60
     * @param zNear, for default, use 0.01
     * @param zFar,  for default, use 1000
     * @return PerspectiveCamera
     */
    public PerspectiveCamera(GLFWWindow window, float fov, float zNear, float zFar) {
        super(window);
        projectionMatrixUpdater = () -> {
            float aspect = window.getWindowAspect();
            projectionMatrix.setPerspective(
                    (float) java.lang.Math.toRadians(fov),
                    aspect,
                    zNear,
                    zFar
            );
            projectionMatrixChanged = true;
        };
    }
}
