package spck.engine;

import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.Measure;
import spck.engine.ecs.ECS;
import spck.engine.ecs.EntityBatchStore;
import spck.engine.ecs.render.PreRenderSystem;
import spck.engine.ecs.render.RenderSystem;
import spck.engine.framework.OpenGLAABBGPUDataStore;
import spck.engine.framework.OpenGLDefaultGPUMeshDataStore;
import spck.engine.framework.OpenGLDefaultMaterialRenderer;
import spck.engine.render.camera.Camera;
import spck.engine.ui.UIRendererSystem;
import spck.engine.window.GLFWWindow;
import spck.engine.window.Input;

import java.lang.reflect.InvocationTargetException;

public class Engine implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Engine.class);
    private final Thread GAME_LOOP_THREAD;
    private final GameLoop gameLoop;
    private final GLFWWindow window;
    private final Input input;
    private final ECS ecs;
    private final EnginePreferences preferences;
    private final Camera camera;

    public Engine(EnginePreferences preferences, Camera camera, GLFWWindow window, Input input) {
        this.preferences = preferences;
        this.camera = camera;
        this.window = window;
        this.input = input;
        this.ecs = new ECS();

        gameLoop = new GameLoop(window, preferences.clearColor);
        this.GAME_LOOP_THREAD = new Thread(this, "GAME_LOOP_THREAD");
    }

    public void launch(Class<? extends AbstractGame> gameClass) {
        try {
            gameClass.getDeclaredConstructor(
                    Camera.class,
                    GLFWWindow.class,
                    Input.class,
                    ECS.class
            ).newInstance(
                    camera,
                    window,
                    input,
                    ecs
            );
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        log.debug("Launching game...");
        log.debug("OS name: {}", System.getProperty("os.name"));
        log.debug("OS version: {}", System.getProperty("os.version"));
        log.debug("Java version: {}, {}", System.getProperty("java.version"), System.getProperty("java.vendor"));
        log.debug("LWJGL version: {}", org.lwjgl.Version.getVersion());
        log.debug("Engine preferences: {}", preferences);
        log.debug("Camera: {}", camera);
        log.debug("Creating GAME_LOOP_THREAD...");

        EntityBatchStore batchStore = new EntityBatchStore(
                new OpenGLDefaultGPUMeshDataStore(),
                new OpenGLAABBGPUDataStore()
        );

        ecs.add(new PreRenderSystem(batchStore));
        ecs.add(new RenderSystem(new OpenGLDefaultMaterialRenderer(), batchStore, camera));
        ecs.add(new UIRendererSystem(preferences.defaultFont, window, input));

        new Measure();

        if (Platform.get() == Platform.MACOSX) {
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
