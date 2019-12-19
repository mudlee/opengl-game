package spck.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.Stats;
import spck.engine.framework.Graphics;
import spck.engine.framework.OpenGLWindow;

class GameLoop {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoop.class);
    private final static int TARGET_FPS = 100;
    private final OpenGLWindow window;

    GameLoop(OpenGLWindow window) {
        this.window = window;
    }

    void loop() {
        LOGGER.debug("Running game loop...");
        double lastLoopTime = Time.getJVMTimeInSec();

        while (window.shouldNotClose()) {
            Stats.reset();
            Graphics.clearScreen(Engine.preferences.clearColor.x, Engine.preferences.clearColor.y, Engine.preferences.clearColor.z, Engine.preferences.clearColor.w);

            double currentTime = Time.getJVMTimeInSec();
            Time.deltaTime = (float) (currentTime - lastLoopTime);
            lastLoopTime = currentTime;

            MessageBus.broadcast(LifeCycle.FRAME_START.eventID());
            MessageBus.broadcast(LifeCycle.UPDATE.eventID());
            MessageBus.broadcast(LifeCycle.AFTER_UPDATE.eventID());
            MessageBus.broadcast(LifeCycle.BEFORE_BUFFER_SWAP.eventID()); // camera resets here
            window.swapBuffers();
            MessageBus.broadcast(LifeCycle.AFTER_BUFFER_SWAP.eventID());

            MessageBus.broadcast(LifeCycle.BEFORE_FRAME_SYNC.eventID());
            if (!window.getPreferences().isvSyncEnabled()) {
                if (window.getPreferences().isLimitFPS()) {
                    sync(lastLoopTime);
                }
            }
            MessageBus.broadcast(LifeCycle.AFTER_FRAME_SYNC.eventID());

        }
        LOGGER.debug("Game loop finished");
    }

    private void sync(double lastLoopTime) {
        float loopSlot = 1f / TARGET_FPS;
        double endTime = lastLoopTime + loopSlot;
        while (Time.getJVMTimeInSec() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
            }
        }
    }
}
