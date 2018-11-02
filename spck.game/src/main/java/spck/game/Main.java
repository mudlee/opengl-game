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

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class Main {
    private final Camera camera = Camera.perspective(60.0f, 01f, 1000f);
    private Cube cube;
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
        camera.setPosition(new Vector3f(0, 0, 10));
        camera.setRotation(new Vector3f(0, 0, 0));

        LightSystem.setAmbientLight(new AmbientLight(new Vector4f(1, 1, 1, 1), 0.4f));

        cube = new Cube();
        cube.getComponent(RenderComponent.class).ifPresent(component -> {
            component.transform.setRotation(new Vector3f(10, 20, 0));
        });

        float speed = 0.1f;

        MessageBus.register(KeyEvent.keyHeldDown(GLFW_KEY_R), () -> {
            Vector3f current = new Vector3f(camera.getPosition());
            current.y -= speed;
            cube.getComponent(RenderComponent.class).ifPresent(component -> {
                Vector3f rotation = component.transform.getRotation();
                rotation.y += 2;
                component.transform.setRotation(rotation);
            });
        });

        MessageBus.register(KeyEvent.pressed(GLFW_KEY_D), () -> cube.destroy());
        MessageBus.register(KeyEvent.pressed(GLFW_KEY_A), this::addCube);
    }

    private void addCube() {
        Cube cube = new Cube();
        cube.getComponent(RenderComponent.class).ifPresent(component -> {
            component.transform.setRotation(new Vector3f(20, 20, 0));
            component.material.setDiffuseColor(new Vector3f(1, 0, 0));
            component.transform.setPosition(x, 0, 0);
            x += -.2;
        });
    }
}
