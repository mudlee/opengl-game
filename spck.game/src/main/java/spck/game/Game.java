package spck.game;

import org.joml.Vector2d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import spck.engine.AbstractGame;
import spck.engine.Engine;
import spck.engine.debug.DebugInputListener;
import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.lights.AmbientLight;
import spck.engine.lights.LightSystem;
import spck.engine.model.primitives.Cube;
import spck.engine.physics.Physics;
import spck.engine.render.camera.Camera;
import spck.engine.render.camera.OrthoCamera;
import spck.engine.window.GLFWWindow;
import spck.engine.window.Input;
import spck.game.nations.NationsEntity;
import spck.game.ui.debug.StatUITextSystem;
import spck.game.ui.debug.StatusUICanvasEntity;
import spck.game.ui.debug.StatusUICanvasRendererSystem;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Game extends AbstractGame {
	public Game(Camera camera, GLFWWindow window, Input input) {
		super(camera, window, input);
	}

	@Override
	protected void onStart() {
		new DebugInputListener(camera, input);

		camera.setPosition(new Vector3f(0, 0, 10));

		window.setMousePosition(new Vector2d(
				(double) window.getWidth() / 2,
				(double) window.getHeight() / 2
		));

		Engine.ecs.add(new StatUITextSystem((OrthoCamera) camera, window));
		Engine.ecs.add(new StatusUICanvasRendererSystem(Engine.uiRenderer, window));
		Engine.ecs.createWorld();


		Entity.create(new GameCameraController((OrthoCamera) camera, window, input));

		LightSystem.setAmbientLight(new AmbientLight(new Vector4f(1, 1, 1, 1), 0.9f));

        /*Tree tree = new Tree();
        Entity.create(tree);
        tree.getComponent(RenderComponent.class).ifPresent(renderer -> {
            renderer.transform.setPosition(new Vector3f(0, 5, 0));
        });*/

		input.onMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT, event -> {
			Physics.raycast(camera.getRay(), 20f).ifPresent(hit -> {
				Cube cube = new Cube();
				Entity.create(cube);
				cube.getComponent(RenderComponent.class).ifPresent(comp -> {
					comp.transform.setScale(new Vector3f(0.01f, 0.01f, 0.01f));
					comp.transform.setPosition(hit.getPosition());
				});
			});
		});

		Entity.create(new WorldMap());
		Entity.create(new CrossHair(window));
		Entity.create(new NationsEntity((GameCamera) camera, window));
		Entity.create(new StatusUICanvasEntity(window));
	}
}
