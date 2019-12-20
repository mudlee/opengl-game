package spck.engine;

import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.render.camera.Camera;
import spck.engine.window.GLFWWindow;
import spck.engine.window.Input;

public abstract class AbstractGame {
	protected final Camera camera;
	protected final GLFWWindow window;
	protected final Input input;

	public AbstractGame(Camera camera, GLFWWindow window, Input input) {
		this.camera = camera;
		this.window = window;
		this.input = input;
		MessageBus.register(LifeCycle.GAME_START.eventID(), this::onStart);
	}

	abstract protected void onStart();
}
