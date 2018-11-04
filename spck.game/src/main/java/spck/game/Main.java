package spck.game;

import org.joml.Vector3f;
import org.joml.Vector4f;
import spck.engine.Engine;
import spck.engine.bus.KeyEvent;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.DebugInputListener;
import spck.engine.debug.FreeCameraController;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.lights.AmbientLight;
import spck.engine.lights.DirectionalLight;
import spck.engine.lights.LightSystem;
import spck.engine.model.primitives.Cube;
import spck.engine.render.Camera;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class Main {
    private final Camera camera = Camera.perspective(60.0f, 01f, 1000f);
    private static final int CUBES = 50000;
    private Cube[] cubes = new Cube[CUBES];

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        Engine engine = new Engine(camera);
        MessageBus.register(LifeCycle.GAME_START.eventID(), this::start);

        engine.launch();
    }

    private void start() {
        new DebugInputListener(camera);
        new FreeCameraController(camera);

        camera.setPosition(new Vector3f(50, 50, 150));

        LightSystem.setAmbientLight(new AmbientLight(new Vector4f(1, 1, 1, 1), 0.4f));
        LightSystem.addLight(new DirectionalLight(
                new Vector4f(1, 1, 1, 1),
                0.7f,
                new Vector3f(40, 20, 10)
        ));

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
            cube.getComponent(RenderComponent.class).ifPresent(component -> {
                //component.material.setDiffuseColor(new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat()));
                component.material.setDiffuseColor(new Vector3f(0.5f, 0.2f, 0.7f));
                component.transform.setPosition(random.nextInt((1000) + 1), random.nextInt((1000) + 1), random.nextInt((1000) + 1));
            });
            cubes[i] = cube;
        }

        Engine.window.captureMouse();
    }
}
