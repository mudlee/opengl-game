package spck.engine.render.camera;

import spck.engine.Engine;

public class OrthoCamera extends AbstractCamera implements Camera {
    private float size;
    private float zNear;
    private float zFar;

    /**
     * Initialising an ortho projection.
     *
     * @param size,  for default, use 10
     * @param zNear, for default, use 0
     * @param zFar,  for default, use 1000
     * @return OrthoCamera
     */
    public OrthoCamera(float size, float zNear, float zFar) {
        super();
        this.size = size;
        this.zNear = zNear;
        this.zFar = zFar;
        projectionMatrixUpdater = () -> {
            float aspect = Engine.window.getPreferences().getWindowAspect();
            projectionMatrix.setOrtho(-this.size * aspect, this.size * aspect, -this.size, this.size, this.zNear, this.zFar);
            projectionMatrixChanged = true;
        };
    }

    public void setSize(float size) {
        this.size = size;
        projectionMatrixUpdater.run();
        viewMatrixChanged = true;
    }

    public void setzNear(float zNear) {
        this.zNear = zNear;
        projectionMatrixUpdater.run();
    }

    public void setzFar(float zFar) {
        this.zFar = zFar;
        projectionMatrixUpdater.run();
    }

    public float getSize() {
        return size;
    }

    public float getzNear() {
        return zNear;
    }

    public float getzFar() {
        return zFar;
    }
}
