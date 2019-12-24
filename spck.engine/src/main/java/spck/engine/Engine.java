package spck.engine;

import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.Measure;
import spck.engine.ecs.ECS;
import spck.engine.ecs.EntityBatchStore;
import spck.engine.ecs.render.PreRenderSystem;
import spck.engine.ecs.render.RenderSystem;
import spck.engine.ecs.ui.UICanvasRendererSystem;
import spck.engine.framework.*;
import spck.engine.render.camera.Camera;
import spck.engine.ui.UIRendererSystem;
import spck.engine.util.OSNameParser;
import spck.engine.window.GLFWWindow;
import spck.engine.window.Input;

import java.lang.reflect.InvocationTargetException;

public class Engine implements Runnable {
    public static final Preferences preferences = new Preferences();
    public static OpenGLDefaultGPUMeshDataStore gpuDataStore;
    public static OpenGLAABBGPUDataStore aabbGpuDataStore;
    public static OpenGLStandardShader shader;

    public static class Preferences {
        public String defaultFont = "GeosansLight";
        public boolean polygonRenderMode;
        public boolean renderAABB = true;
        public OS os;
        public Vector4f clearColor = new Vector4f(1f, 1f, 1f, 0f);

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

    private static final Logger log = LoggerFactory.getLogger(Engine.class);
    private final Thread GAME_LOOP_THREAD;
    private final GameLoop gameLoop;
    private final GLFWWindow window;
    private final Input input;
    private final ECS ecs;
    private final Camera camera;
    private final UIRenderer uiRenderer;

    public Engine(Camera camera, GLFWWindow window, Input input) {
        this.camera = camera;
        this.window = window;
        this.input = input;
        this.ecs = new ECS();
        this.uiRenderer = new UIRenderer(Engine.preferences.defaultFont);

        String osName = System.getProperty("os.name");
        preferences.os = OSNameParser.parse(osName);

        gameLoop = new GameLoop(window);
        this.GAME_LOOP_THREAD = new Thread(this, "GAME_LOOP_THREAD");
    }

    public void launch(Class<? extends AbstractGame> gameClass) {
        try {
            gameClass.getDeclaredConstructor(
                    Camera.class,
                    GLFWWindow.class,
                    Input.class,
                    ECS.class,
                    UIRenderer.class
            ).newInstance(
                    camera,
                    window,
                    input,
                    ecs,
                    uiRenderer
            );
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        log.debug("Launching game...");

        gpuDataStore = new OpenGLDefaultGPUMeshDataStore();
        aabbGpuDataStore = new OpenGLAABBGPUDataStore();
        shader = new OpenGLStandardShader(camera);

        log.debug("OS name: {}", System.getProperty("os.name"));
        log.debug("OS version: {}", System.getProperty("os.version"));
        log.debug("Java version: {}, {}", System.getProperty("java.version"), System.getProperty("java.vendor"));
        log.debug("LWJGL version: {}", org.lwjgl.Version.getVersion());
        log.debug("Engine preferences: {}", preferences);
        log.debug("Camera: {}", camera);
        log.debug("Creating GAME_LOOP_THREAD...");


        //new NuklearHandler(window);

        EntityBatchStore batchStore = new EntityBatchStore();

        ecs.add(new PreRenderSystem(batchStore));
        ecs.add(new RenderSystem(new OpenGLDefaultMaterialRenderer(), batchStore, camera));
        ecs.add(new UICanvasRendererSystem(window, uiRenderer));
        ecs.add(new UIRendererSystem(window));

        new Measure();

        if (preferences.os == OS.MACOS) {
            GAME_LOOP_THREAD.run();
        } else {
            GAME_LOOP_THREAD.start();
        }
    }

    @Override
    public void run() {
        window.create();
        input.create(window.getWindowWidth(), window.getWindowHeight(), window.getCursorPositionConsumer());
        MessageBus.broadcast(LifeCycle.GAME_START.eventID());
        gameLoop.loop();
        MessageBus.broadcast(LifeCycle.CLEANUP.eventID());
    }
}
