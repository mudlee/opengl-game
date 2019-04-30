package spck.game;

import org.joml.Vector3f;
import org.joml.Vector4f;
import spck.engine.Antialiasing;
import spck.engine.Engine;
import spck.engine.bus.KeyEvent;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.DebugInputListener;
import spck.engine.debug.FreeCameraController;
import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.framework.RGBAColor;
import spck.engine.framework.Window;
import spck.engine.lights.AmbientLight;
import spck.engine.lights.Attenuation;
import spck.engine.lights.DirectionalLight;
import spck.engine.lights.LightSystem;
import spck.engine.lights.PointLight;
import spck.engine.model.primitives.Cube;
import spck.engine.render.Camera;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class Main {
    private final static Camera CAMERA = Camera.perspective(60.0f, 01f, 10000f);
    private final static Window.Preferences WINDOW_PREFERENCES = new Window.Preferences(
            false,
            Antialiasing.ANTIALISING_2X,
            false,
            false);
    private static final int CUBES = 10000;
    private Cube[] cubes = new Cube[CUBES];

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
        new FreeCameraController(CAMERA);

        CAMERA.setPosition(new Vector3f(50, 50, 150));

        LightSystem.setAmbientLight(new AmbientLight(new Vector4f(1, 1, 1, 1), 0.4f));
        LightSystem.addLight(new DirectionalLight(
                new Vector4f(1, 1, 1, 1),
                0.7f,
                new Vector3f(40, 20, 10)
        ));
        Vector3f color = RGBAColor.rgbToVector3f(205, 66, 229);
        LightSystem.addLight(new PointLight(new Vector4f(color.x, color.y, color.z, 1), 100f, new Vector3f(50, 20, 50), Attenuation.distance50()));

        Engine.window.captureMouse();

        //Entity.create(new Terrain());

        Cube cube = new Cube();

        MessageBus.register(KeyEvent.keyHeldDown(GLFW_KEY_R), () -> {
            for (Cube cube1 : cubes) {
                cube1.getComponent(RenderComponent.class).ifPresent(component -> {
                    Vector3f rotation = component.transform.getRotation();
                    rotation.x += 1;
                    rotation.y += 1;
                    rotation.z += 1;
                    component.transform.setRotation(rotation);
                });
            }
        });

        Entity.create(cube).getComponent(RenderComponent.class).ifPresent(component -> {
            component.transform.setScale(new Vector3f(10, 10, 10));
            component.transform.setPosition(new Vector3f(50, 20, 50));
            component.material.setDiffuseColor(RGBAColor.rgbToVector3f(205, 66, 229));

        });

        createCubes();
    }

    private void createCubes() {
        Random random = new Random();
        MessageBus.register(KeyEvent.keyHeldDown(GLFW_KEY_R), () -> {
            for (Cube cube : cubes) {
                cube.getComponent(RenderComponent.class).ifPresent(component -> {
                    Vector3f rotation = component.transform.getRotation();
                    rotation.x += 5;
                    rotation.y += 5;
                    rotation.z += 5;
                    component.transform.setRotation(rotation);
                });
            }
        });

        for (int i = 0; i < CUBES; i++) {
            Cube cube = new Cube();
            Entity.create(cube);
            cube.getComponent(RenderComponent.class).ifPresent(component -> {
                //component.material.setDiffuseColor(new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat()));
                component.material.setDiffuseColor(new Vector3f(0.5f, 0.2f, 0.7f));
                component.transform.setPosition(random.nextInt((200) + 1), random.nextInt((200) + 1), random.nextInt((200) + 1));
            });
            cubes[i] = cube;
        }
    }
}
