package spck.game;

import spck.engine.Engine;
import spck.engine.EnginePreferences;
import spck.engine.window.Antialiasing;
import spck.engine.window.GLFWPreferences;
import spck.engine.window.GLFWWindow;
import spck.engine.window.Input;

public class Main {
	private final static GLFWPreferences GLFW_PREFERENCES = GLFWPreferences
			.Builder
			.create()
			.withAntialiasing(Antialiasing.ANTIALISING_2X)
			.withWidth(2560)
			.withHeight(1440)
			.build();

	private final static EnginePreferences ENGINE_PREFERENCES = EnginePreferences
			.Builder
			.create()
			.build();

	public static void main(String[] args) {
		new Main().run();
	}

	private void run() {
		Input input = new Input();
		GLFWWindow window = new GLFWWindow(GLFW_PREFERENCES, input);
		GameCamera camera = new GameCamera(window, 10, 0, 10000);

		Engine engine = new Engine(ENGINE_PREFERENCES, camera, window, input);
        engine.launch(Game.class);

    }
}
