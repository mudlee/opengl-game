package spck.game;

import org.joml.Vector2d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import spck.engine.Antialiasing;
import spck.engine.Engine;
import spck.engine.Input.Input;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.DebugInputListener;
import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.framework.OpenGLWindow;
import spck.engine.lights.AmbientLight;
import spck.engine.lights.DirectionalLight;
import spck.engine.lights.LightSystem;
import spck.engine.model.primitives.Cube;
import spck.engine.physics.Physics;
import spck.game.ui.debug.StatUITextSystem;
import spck.game.ui.debug.StatusUI;
import spck.game.ui.debug.StatusUICanvasRendererSystem;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Main {
    private final static GameCamera CAMERA = new GameCamera(10, 0, 10000);
    private final static OpenGLWindow.Preferences WINDOW_PREFERENCES = new OpenGLWindow.Preferences(
            false,
            Antialiasing.ANTIALISING_2X,
            false,
            false
    );
    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        WINDOW_PREFERENCES.setWidth(2560);
        WINDOW_PREFERENCES.setHeight(1440);
        Engine engine = new Engine(CAMERA, WINDOW_PREFERENCES);
        MessageBus.register(LifeCycle.GAME_START.eventID(), this::start);

        new StatusUI();
        engine.launch();
    }

    private void start() {
        new DebugInputListener(CAMERA);

        CAMERA.setPosition(new Vector3f(0, 0, 10));
        Input.setMousePosition(new Vector2d(
                (double) Engine.window.getPreferences().getWidth() / 2,
                (double) Engine.window.getPreferences().getHeight() / 2
        ));

        Engine.ecs.add(new StatUITextSystem(CAMERA));
        Engine.ecs.add(new StatusUICanvasRendererSystem(Engine.uiRenderer));
        Engine.ecs.createWorld();


        Entity.create(new GameCameraController(CAMERA));

        LightSystem.setAmbientLight(new AmbientLight(new Vector4f(1, 1, 1, 1), 0.9f));
        LightSystem.addLight(new DirectionalLight(
                new Vector4f(1, 1, 1, 1),
                0.3f,
                new Vector3f(40, 20, 10)
        ));

        Tree tree = new Tree();
        Entity.create(tree);
        tree.getComponent(RenderComponent.class).ifPresent(renderer -> {
            renderer.transform.setPosition(new Vector3f(0, 5, 0));
        });

        Input.onKeyHeldDown(GLFW_KEY_R, event -> {
            tree.getComponent(RenderComponent.class).ifPresent(renderer -> {
                Vector3f rot = renderer.transform.getRotation();
                rot.x += 0.1f;
                renderer.transform.setRotation(rot);
            });
        });


        Input.onMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT, event -> {
            Physics.raycast(CAMERA.getRay(), 20f).ifPresent(hit -> {
                Cube cube = new Cube();
                Entity.create(cube);
                cube.getComponent(RenderComponent.class).ifPresent(comp -> {
                    comp.transform.setScale(new Vector3f(0.01f, 0.01f, 0.01f));
                    comp.transform.setPosition(hit.getPosition());
                });
            });
        });


        Entity.create(new WorldMap());
        Entity.create(new CrossHair());
    }
}
