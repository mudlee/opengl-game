package spck.game;

import spck.engine.render.camera.Camera;
import spck.engine.render.camera.OrthoCamera;
import spck.engine.window.GLFWWindow;

public class GameCamera extends OrthoCamera implements Camera {
    GameCamera(GLFWWindow window, int size, int zNear, int zFar) {
        super(window, size, zNear, zFar);
    }

    @Override
    public void forceUpdate() {
        positionChanged = true;
        viewMatrixChanged = true;
        updateViewMatrix();
    }
}
