package spck.engine;

import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.ecs.ECS;
import spck.engine.framework.UIRenderer;
import spck.engine.render.camera.Camera;
import spck.engine.window.GLFWWindow;
import spck.engine.window.Input;

public abstract class AbstractGame {
	protected final Camera camera;
	protected final GLFWWindow window;
	protected final Input input;
	protected final ECS ecs;
	protected final UIRenderer uiRenderer;

	public AbstractGame(
			Camera camera,
			GLFWWindow window,
			Input input,
			ECS ecs,
			UIRenderer uiRenderer
	) {
		this.camera = camera;
		this.window = window;
		this.input = input;
		this.ecs = ecs;
		this.uiRenderer = uiRenderer;
		MessageBus.register(LifeCycle.GAME_START.eventID(), this::setupECS);
		MessageBus.register(LifeCycle.GAME_START.eventID(), this::onStart);
	}

	abstract protected void registerECSSystems();

	abstract protected void onStart();

	private void setupECS() {
		registerECSSystems();
		ecs.createWorld();
	}
}
