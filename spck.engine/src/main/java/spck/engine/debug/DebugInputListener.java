package spck.engine.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.ecs.render.RenderSystem;
import spck.engine.render.camera.Camera;
import spck.engine.window.Input;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F12;

public class DebugInputListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(DebugInputListener.class);

    public DebugInputListener(Camera camera, Input input) {
        input.onKeyPressed(GLFW_KEY_F12, event -> {
            camera.forceUpdate();

            RenderSystem.polygonRenderMode = !RenderSystem.polygonRenderMode;
            LOGGER.debug("polygonMode has been set to: {}", RenderSystem.polygonRenderMode);
        });

        input.onKeyPressed(GLFW_KEY_F11, event -> {
            RenderSystem.renderAABB = !RenderSystem.renderAABB;
            LOGGER.debug("renderAABB has been set to: {}", RenderSystem.renderAABB);
        });
    }
}
