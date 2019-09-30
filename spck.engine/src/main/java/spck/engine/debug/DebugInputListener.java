package spck.engine.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.Engine;
import spck.engine.bus.KeyEvent;
import spck.engine.bus.MessageBus;
import spck.engine.framework.Graphics;
import spck.engine.render.camera.Camera;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F12;

public class DebugInputListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(DebugInputListener.class);

    public DebugInputListener(Camera camera) {
        MessageBus.register(KeyEvent.pressed(GLFW_KEY_F12), (event) -> {
            camera.forceUpdate();

            Engine.preferences.polygonRenderMode = !Engine.preferences.polygonRenderMode;
            Graphics.setPolygonMode(Engine.preferences.polygonRenderMode ? Graphics.PolygonMode.LINE : Graphics.PolygonMode.FILL);
            LOGGER.debug("polygonMode has been set to: {}", Engine.preferences.polygonRenderMode);
        });
    }
}
