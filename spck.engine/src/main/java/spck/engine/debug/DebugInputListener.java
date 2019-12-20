package spck.engine.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.Engine;
import spck.engine.render.camera.Camera;
import spck.engine.window.Input;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F12;

public class DebugInputListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(DebugInputListener.class);

    public DebugInputListener(Camera camera, Input input) {
        input.onKeyPressed(GLFW_KEY_F12, event -> {
            camera.forceUpdate();

            Engine.preferences.polygonRenderMode = !Engine.preferences.polygonRenderMode;
            LOGGER.debug("polygonMode has been set to: {}", Engine.preferences.polygonRenderMode);
        });

        input.onKeyPressed(GLFW_KEY_F11, event -> {
            Engine.preferences.renderAABB = !Engine.preferences.renderAABB;
            LOGGER.debug("renderAABB has been set to: {}", Engine.preferences.renderAABB);
        });
    }
}
