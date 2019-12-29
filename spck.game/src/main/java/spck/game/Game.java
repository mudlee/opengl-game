package spck.game;

import org.joml.Vector2d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import spck.engine.AbstractGame;
import spck.engine.debug.DebugInputListener;
import spck.engine.ecs.ECS;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.framework.RGBAColor;
import spck.engine.lights.AmbientLight;
import spck.engine.lights.LightSystem;
import spck.engine.model.primitives.Cube;
import spck.engine.physics.Physics;
import spck.engine.render.camera.Camera;
import spck.engine.render.camera.OrthoCamera;
import spck.engine.ui.Align;
import spck.engine.ui.Button;
import spck.engine.ui.Canvas;
import spck.engine.window.GLFWWindow;
import spck.engine.window.Input;
import spck.game.nations.NationsEntity;
import spck.game.ui.DebugUI;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Game extends AbstractGame {
	public Game(Camera camera, GLFWWindow window, Input input, ECS ecs) {
		super(camera, window, input, ecs);
	}

	@Override
	protected void registerECSSystems() {
	}

	@Override
	protected void onStart() {
		new DebugInputListener(camera, input);
		input.onKeyPressed(GLFW_KEY_Q, event -> window.close());

		camera.setPosition(new Vector3f(0, 0, 10));

		window.setMousePosition(new Vector2d(
				(double) window.getWindowWidth() / 2,
				(double) window.getWindowHeight() / 2
		));

		Canvas canvas = (Canvas) ecs.createEntity(new Canvas());
		ecs.createEntity(new GameCameraController((OrthoCamera) camera, window, input, canvas));

		LightSystem.setAmbientLight(new AmbientLight(new Vector4f(1, 1, 1, 1), 0.9f));

		input.onMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT, event -> {
			Physics.raycast(camera.getRay(), 20f).ifPresent(hit -> {
				Cube cube = new Cube();
				ecs.createEntity(cube);
				cube.getComponent(RenderComponent.class).ifPresent(comp -> {
					comp.transform.setScale(new Vector3f(0.01f, 0.01f, 0.01f));
					comp.transform.setPosition(hit.getPosition());
				});
			});
		});

		ecs.createEntity(new WorldMap());
		ecs.createEntity(new CrossHairCanvas(window, canvas));
		ecs.createEntity(new NationsEntity((GameCamera) camera, canvas));

		new DebugUI(ecs, window, (OrthoCamera) camera);

		canvas.addButton(Button.Builder
				.create(100, 50, "QUIT")
				.withCornerRadius(5)
				.withAlign(Align.MIDDLE_CENTER)
				.withOnClickHandler(this::onQuitButtonClick)
				.withBackgroundMouseOverColor(RGBAColor.red())
				.build()
		);
	}

	private void onQuitButtonClick() {
		window.close();
	}
}
