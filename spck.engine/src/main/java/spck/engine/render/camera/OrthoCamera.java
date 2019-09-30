package spck.engine.render.camera;

import spck.engine.Engine;

public class OrthoCamera extends AbstractCamera implements Camera {
    /**
     * Initialising an ortho projection.
     *
     * @param size,  for default, use 10
     * @param zNear, for default, use 0
     * @param zFar,  for default, use 1000
     * @return Camera
     */
    public OrthoCamera(int size, int zNear, int zFar) {
        super();
        projectionMatrixUpdater = () -> {
            float aspect = Engine.window.getPreferences().getWindowAspect();
            projectionMatrix.setOrtho(-size * aspect, size * aspect, -size, size, zNear, zFar);
            projectionMatrixChanged = true;
        };
    }
}
