package spck.engine;

import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.Measure;
import spck.engine.ecs.ECS;
import spck.engine.ecs.debug.StatUIEntitiesBuilder;
import spck.engine.ecs.debug.StatUITextSystem;
import spck.engine.ecs.ui.UIRendererSystem;
import spck.engine.framework.Window;
import spck.engine.util.OSNameParser;

import java.util.Arrays;

public class Engine implements Runnable{
    public static final Preferences preferences = new Preferences();
    public static Window window;

    public static class Preferences {
        public String defaultFont = "GeosansLight";
        public boolean polygonRenderMode;
        public OS os;
        public Vector4f clearColor = new Vector4f(1f, 1f, 1f, 1f);

    }
    private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    private final Thread GAME_LOOP_THREAD;
    private final GameLoop gameLoop;
    private final ECS ecs;

    public Engine() {
        String osName = System.getProperty("os.name");
        preferences.os = OSNameParser.parse(osName);

        LOGGER.debug("OS name: {}", osName);
        LOGGER.debug("OS version: {}", System.getProperty("os.version"));
        LOGGER.debug("Java version: {}, {}", System.getProperty("java.version"), System.getProperty("java.vendor"));
        LOGGER.debug("LWJGL version: {}", org.lwjgl.Version.getVersion());
        //LOGGER.debug("Engine properties: {}", EngineProperties.print());
        //LOGGER.debug("Main camera: {}", Camera.main);
        LOGGER.debug("Creating GAME_LOOP_THREAD...");
        this.GAME_LOOP_THREAD=new Thread(this,"GAME_LOOP_THREAD");

        window = new Window(new Window.Preferences(
                true,
                Antialiasing.ANTIALISING_2X,
                false
        ));


        ecs = new ECS(Arrays.asList(
                new StatUITextSystem(),
                new UIRendererSystem()
        ));

        new StatUIEntitiesBuilder().build();

        gameLoop = new GameLoop(window);
    }

    public void launch() {
        LOGGER.debug("Launching game...");

        new Measure();

        if (preferences.os == OS.MACOS) {
            GAME_LOOP_THREAD.run();
        }
        else {
            GAME_LOOP_THREAD.start();
        }
    }

    @Override
    public void run() {
        MessageBus.broadcast(LifeCycle.GAME_START.eventID());
        gameLoop.loop();
        MessageBus.broadcast(LifeCycle.CLEANUP.eventID());
    }
}
