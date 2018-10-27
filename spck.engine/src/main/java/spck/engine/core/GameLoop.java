package spck.engine.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.framework.GLFWOpenGLWindow;
import spck.engine.util.Expect;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

public class GameLoop {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoop.class);

    public void loop() {
        LOGGER.debug("Running game loop...");
        while (!glfwWindowShouldClose(Expect.notNull(GLFWOpenGLWindow.ID, "Window ID is null"))) {
            MessageBus.broadcast(LifeCycle.UPDATE.eventID(), null);
            glfwPollEvents();
        }
        LOGGER.debug("Game loop finished");
    }
}
