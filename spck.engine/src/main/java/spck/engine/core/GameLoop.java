package spck.engine.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL41;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.Engine;
import spck.engine.Time;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.Stats;
import spck.engine.framework.GLFWOpenGLWindow;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

public class GameLoop {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoop.class);
    private final static int TARGET_FPS = 100;
    private final GLFWOpenGLWindow window;

    public GameLoop(GLFWOpenGLWindow window) {
        this.window = window;
    }

    public void loop() {
        LOGGER.debug("Running game loop...");
        double lastLoopTime = Time.getTimeInSec();

        while (!glfwWindowShouldClose(window.getID())) {
            Stats.reset();
            GL41.glClearColor(Engine.preferences.clearColor.x, Engine.preferences.clearColor.y, Engine.preferences.clearColor.z, Engine.preferences.clearColor.w);
            GL41.glClear(GL41.GL_COLOR_BUFFER_BIT | GL41.GL_DEPTH_BUFFER_BIT | GL41.GL_STENCIL_BUFFER_BIT);

            double currentTime = Time.getTimeInSec();
            Time.deltaTime = (float) (currentTime - lastLoopTime);
            lastLoopTime = currentTime;

            MessageBus.broadcast(LifeCycle.FRAME_START.eventID());
            MessageBus.broadcast(LifeCycle.UPDATE.eventID());

            // ecs

            MessageBus.broadcast(LifeCycle.BEFORE_BUFFER_SWAP.eventID());

            GLFW.glfwSwapBuffers(window.getID());
            MessageBus.broadcast(LifeCycle.AFTER_BUFFER_SWAP.eventID());

            glfwPollEvents();

            MessageBus.broadcast(LifeCycle.BEFORE_FRAME_SYNC.eventID());
            if (!window.getPreferences().isvSyncEnabled()) {
                sync(lastLoopTime);
            }
            MessageBus.broadcast(LifeCycle.AFTER_FRAME_SYNC.eventID());

        }
        LOGGER.debug("Game loop finished");
    }

    private void sync(double lastLoopTime) {
        float loopSlot = 1f / TARGET_FPS;
        double endTime = lastLoopTime + loopSlot;
        while (Time.getTimeInSec() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
            }
        }
    }
}
