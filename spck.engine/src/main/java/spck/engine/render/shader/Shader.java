package spck.engine.render.shader;

import spck.engine.render.Material;

public interface Shader {
	void init();

	boolean isInitialized();

	void startShader(Material material);

	void stopShader();
}
