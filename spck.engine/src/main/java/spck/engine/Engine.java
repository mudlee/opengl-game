package spck.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.core.GameLoop;
import spck.engine.core.OS;
import spck.engine.framework.GLFWOpenGLWindow;
import spck.engine.graphics.Antialiasing;
import spck.engine.util.OSNameParser;

public class Engine implements Runnable{
    public static final Preferences PREFERENCES = new Preferences();

    public static class Preferences {
        public String defaultFont = "GeosansLight";
        public boolean polygonRenderMode;
        public OS os;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    private final Thread GAME_LOOP_THREAD;
    private final GameLoop gameLoop=new GameLoop();

    public Engine() {
        String osName = System.getProperty("os.name");
        PREFERENCES.os = OSNameParser.parse(osName);

        LOGGER.debug("OS name: {}", osName);
        LOGGER.debug("OS version: {}", System.getProperty("os.version"));
        LOGGER.debug("Java version: {}, {}", System.getProperty("java.version"), System.getProperty("java.vendor"));
        LOGGER.debug("LWJGL version: {}", org.lwjgl.Version.getVersion());
        //LOGGER.debug("Engine properties: {}", EngineProperties.print());
        //LOGGER.debug("Main camera: {}", Camera.main);
        LOGGER.debug("Creating GAME_LOOP_THREAD...");
        this.GAME_LOOP_THREAD=new Thread(this,"GAME_LOOP_THREAD");
    }

    public void launch() {
        LOGGER.debug("Launching game...");

        new GLFWOpenGLWindow(new GLFWOpenGLWindow.Preferences(true, Antialiasing.ANTIALISING_2X, false));

        if (PREFERENCES.os == OS.MACOS) {
            GAME_LOOP_THREAD.run();
        }
        else {
            GAME_LOOP_THREAD.start();
        }
    }

    @Override
    public void run() {
        MessageBus.broadcast(LifeCycle.START.eventID(), null);
        gameLoop.loop();
        MessageBus.broadcast(LifeCycle.CLEANUP.eventID(), null);
    }
}
