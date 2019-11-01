package spck.game;

import org.joml.Vector3f;
import org.joml.Vector4f;
import spck.engine.Antialiasing;
import spck.engine.Engine;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.DebugInputListener;
import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.framework.Window;
import spck.engine.lights.AmbientLight;
import spck.engine.lights.DirectionalLight;
import spck.engine.lights.LightSystem;
import spck.engine.render.camera.Camera;

public class Main {
    private final static Camera CAMERA = new RPGCamera(60.0f, 0.1f, 10000f);
    private final static Window.Preferences WINDOW_PREFERENCES = new Window.Preferences(
            false,
            Antialiasing.ANTIALISING_2X,
            false,
            false
    );
    private static final int CUBES = 10000;

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        WINDOW_PREFERENCES.setWidth(2560);
        WINDOW_PREFERENCES.setHeight(1440);
        Engine engine = new Engine(CAMERA, WINDOW_PREFERENCES);
        MessageBus.register(LifeCycle.GAME_START.eventID(), this::start);

        engine.launch();
    }

    private void start() {
        new DebugInputListener(CAMERA);


        CAMERA.setPosition(new Vector3f(-3, 11, 3));
        CAMERA.setRotation(new Vector3f(-50, 45, 0));
        Entity.create(new RPGCameraController(CAMERA));

        LightSystem.setAmbientLight(new AmbientLight(new Vector4f(1, 1, 1, 1), 0.9f));
        LightSystem.addLight(new DirectionalLight(
                new Vector4f(1, 1, 1, 1),
                0.3f,
                new Vector3f(40, 20, 10)
        ));

        Entity.create(new Ground());
        Tree tree = new Tree();
        Entity.create(tree);
        tree.getComponent(RenderComponent.class).ifPresent(renderer -> {
            renderer.transform.setScale(new Vector3f(0.3f, 0.3f, 0.3f));
            renderer.transform.setPosition(new Vector3f(0, 2.5f, 0));
        });

        Entity castle = Entity.create(new Castle());
        castle.getComponent(RenderComponent.class).ifPresent(castleRender -> {
            castleRender.transform.setPosition(new Vector3f(0, 1, 0));
        });
    }
}
