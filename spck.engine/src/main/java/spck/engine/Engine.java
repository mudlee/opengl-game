package spck.engine;

import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.Input.Input;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.Measure;
import spck.engine.ecs.ECS;
import spck.engine.ecs.EntityBatchStore;
import spck.engine.ecs.debug.StatUITextSystem;
import spck.engine.ecs.debug.StatusUI;
import spck.engine.ecs.debug.StatusUICanvasRendererSystem;
import spck.engine.ecs.render.PreRenderSystem;
import spck.engine.ecs.render.RenderSystem;
import spck.engine.ecs.ui.UICanvasRendererSystem;
import spck.engine.framework.*;
import spck.engine.render.camera.Camera;
import spck.engine.util.OSNameParser;

import java.util.Arrays;

public class Engine implements Runnable{
    public static final Preferences preferences = new Preferences();
    public static Window window;
    public static OpenGLDefaultGPUDataStore gpuDataStore;
    public static OpenGLAABBGPUDataStore aabbGpuDataStore;
    public static OpenGLStandardShader shader;
    public static Renderer renderer;

    public static class Preferences {
        public String defaultFont = "GeosansLight";
        public boolean polygonRenderMode;
        public boolean renderAABB = true;
        public OS os;
        public Vector4f clearColor = new Vector4f(0f, 0f, 0f, 0f);

        @Override
        public String toString() {
            return "Preferences{" +
                    "defaultFont='" + defaultFont + '\'' +
                    ", polygonRenderMode=" + polygonRenderMode +
                    ", os=" + os +
                    ", clearColor=" + clearColor +
                    '}';
        }
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    private final Thread GAME_LOOP_THREAD;
    private final GameLoop gameLoop;

    public Engine(Camera camera, Window.Preferences windowPreferences) {
        String osName = System.getProperty("os.name");
        preferences.os = OSNameParser.parse(osName);

        gpuDataStore = new OpenGLDefaultGPUDataStore();
        aabbGpuDataStore = new OpenGLAABBGPUDataStore();
        shader = new OpenGLStandardShader(camera);
        renderer = new OpenGLDefaultMaterialRenderer();

        LOGGER.debug("OS name: {}", osName);
        LOGGER.debug("OS version: {}", System.getProperty("os.version"));
        LOGGER.debug("Java version: {}, {}", System.getProperty("java.version"), System.getProperty("java.vendor"));
        LOGGER.debug("LWJGL version: {}", org.lwjgl.Version.getVersion());
        LOGGER.debug("Engine preferences: {}", preferences);
        LOGGER.debug("Camera: {}", camera);
        LOGGER.debug("Creating GAME_LOOP_THREAD...");
        this.GAME_LOOP_THREAD=new Thread(this,"GAME_LOOP_THREAD");

        window = new Window(windowPreferences);
        Input.initialize();
        new StatusUI();
        LOGGER.debug("Window preferences: {}", window.getPreferences());

        EntityBatchStore batchStore = new EntityBatchStore();
        UIRenderer uiRenderer = new UIRenderer(Engine.preferences.defaultFont);

        new ECS(Arrays.asList(
                new PreRenderSystem(batchStore),
                new RenderSystem(batchStore, camera),
                new StatUITextSystem(camera),
                new UICanvasRendererSystem(uiRenderer),
                new StatusUICanvasRendererSystem(uiRenderer)
        ));

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
