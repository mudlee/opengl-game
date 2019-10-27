package spck.game;

import org.joml.Vector3f;
import org.joml.Vector4f;
import spck.engine.Antialiasing;
import spck.engine.Engine;
import spck.engine.bus.KeyEvent;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.DebugInputListener;
import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.framework.Window;
import spck.engine.lights.AmbientLight;
import spck.engine.lights.DirectionalLight;
import spck.engine.lights.LightSystem;
import spck.engine.model.primitives.Cube;
import spck.engine.render.camera.Camera;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class Main {
    private final static Camera CAMERA = new RPGCamera(60.0f, 0.1f, 10000f);
    private final static Window.Preferences WINDOW_PREFERENCES = new Window.Preferences(
            false,
            Antialiasing.ANTIALISING_2X,
            false,
            false
    );
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


        //CAMERA.setPosition(new Vector3f(-3, 11, 3));
        //CAMERA.setRotation(new Vector3f(-50, -45, 0));
        CAMERA.setPosition(new Vector3f(-3, 11, 3));
        CAMERA.setRotation(new Vector3f(-50, 45, 0));
        Entity.create(new RPGCameraController(CAMERA));
        //new FreeCameraController(CAMERA);
        //

        LightSystem.setAmbientLight(new AmbientLight(new Vector4f(1, 1, 1, 1), 0.9f));
        LightSystem.addLight(new DirectionalLight(
                new Vector4f(1, 1, 1, 1),
                0.3f,
                new Vector3f(40, 20, 10)
        ));

        //Entity.create(new Tree());
        Entity.create(new Ground());


        Entity castle = Entity.create(new Castle());
        castle.getComponent(RenderComponent.class).ifPresent(castleRender -> {
            castleRender.transform.setPosition(new Vector3f(0, 1, 0));
        });

        //Entity.create(new Terrain());
        //createCubes();
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
                //component.material.setDiffuseColor(new Vector3f(0.5f, 0.2f, 0.7f));
                component.transform.setPosition(random.nextInt((200) + 1), random.nextInt((200) + 1), random.nextInt((200) + 1));
            });
            cubes[i] = cube;
        }

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
    }
}
