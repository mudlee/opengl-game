package spck.game;

import spck.engine.render.camera.Camera;
import spck.engine.render.camera.OrthoCamera;

public class GameCamera extends OrthoCamera implements Camera {
    GameCamera(int size, int zNear, int zFar) {
        super(size, zNear, zFar);
    }

    @Override
    public void forceUpdate() {
        positionChanged = true;
        viewMatrixChanged = true;
        updateViewMatrix();
    }
}
