package spck.game;

import org.joml.Vector3f;
import org.joml.Vector4f;
import spck.engine.Engine;
import spck.engine.bus.KeyEvent;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.DebugInputListener;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.lights.AmbientLight;
import spck.engine.lights.LightSystem;
import spck.engine.model.primitives.Cube;
import spck.engine.render.Camera;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class Main {
    private final Camera camera = Camera.perspective(60.0f, 01f, 1000f);
    private Cube[] cubes = new Cube[2000];
    private static float x = 0f;

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        MessageBus.register(LifeCycle.GAME_START.eventID(), this::start);
        new Engine(camera).launch();
    }

    private void start() {
        new DebugInputListener(camera);
        camera.setPosition(new Vector3f(50, 50, 150));
        camera.setRotation(new Vector3f(0, 0, 0));

        LightSystem.setAmbientLight(new AmbientLight(new Vector4f(1, 1, 1, 1), 0.4f));

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

        for (int i = 0; i < 2000; i++) {
            Cube cube = new Cube();
            cube.getComponent(RenderComponent.class).ifPresent(component -> {
                component.transform.setRotation(new Vector3f(20, 20, 0));
                component.material.setDiffuseColor(new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat()));
                component.transform.setPosition(random.nextInt((100) + 1), random.nextInt((100) + 1), random.nextInt((100) + 1));
            });
            cubes[i] = cube;
        }
    }
}
