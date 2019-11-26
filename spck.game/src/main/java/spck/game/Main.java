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

import java.nio.ByteBuffer;

import static java.lang.Math.round;
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

        engine.launch();
    }

    private void start() {
        new DebugInputListener(CAMERA);

        CAMERA.setPosition(new Vector3f(-3, 11, 10));
        Input.setMousePosition(new Vector2d(
                (double) Engine.window.getPreferences().getWidth() / 2,
                (double) Engine.window.getPreferences().getHeight() / 2
        ));
        Entity.create(new GameCameraController(CAMERA));

        LightSystem.setAmbientLight(new AmbientLight(new Vector4f(1, 1, 1, 1), 0.9f));
        LightSystem.addLight(new DirectionalLight(
                new Vector4f(1, 1, 1, 1),
                0.3f,
                new Vector3f(40, 20, 10)
        ));

        /*Entity.create(new Ground());
        Tree tree = new Tree();
        Entity.create(tree);
        tree.getComponent(RenderComponent.class).ifPresent(renderer -> {
            renderer.transform.setScale(new Vector3f(0.3f, 0.3f, 0.3f));
            renderer.transform.setPosition(new Vector3f(0, 2.5f, 0));
        });

        Castle castle = new Castle();
        Entity castleE = Entity.create(castle);
        castleE.getComponent(RenderComponent.class).ifPresent(castleRender -> {
            castleRender.transform.setPosition(new Vector3f(20, 1, 0));
        });*/

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


        Entity.create(new Map());
        Entity.create(new CrossHair());
    }

    private static void premultiplyAlpha(ByteBuffer image, int w, int h, int stride) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * stride + x * 4;

                float alpha = (image.get(i + 3) & 0xFF) / 255.0f;
                image.put(i + 0, (byte) round(((image.get(i + 0) & 0xFF) * alpha)));
                image.put(i + 1, (byte) round(((image.get(i + 1) & 0xFF) * alpha)));
                image.put(i + 2, (byte) round(((image.get(i + 2) & 0xFF) * alpha)));
            }
        }
    }
}
