package spck.engine.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

public class GameLoop {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoop.class);
    private long windowID;

    public void init(long windowID) {
        this.windowID = windowID;
    }

    public void loop() {
        LOGGER.debug("Running game loop...");
        while (!glfwWindowShouldClose(windowID)) {
            glfwPollEvents();
        }
        LOGGER.debug("Game loop finished");
    }
}
