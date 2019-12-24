package spck.engine.ui;

import org.lwjgl.opengl.GL41;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.render.Material;
import spck.engine.render.shader.AbstractShader;
import spck.engine.render.shader.Shader;
import spck.engine.util.ResourceLoader;
import spck.engine.window.GLFWWindow;

import static org.lwjgl.system.MemoryStack.stackPush;

// TODO using program pipleines from AbstractShader somewhy does not work. Investigate it.
public class UIShader extends AbstractShader implements Shader {
	private final GLFWWindow window;

	private enum Uniform {
		TEXTURE,
		PROJECTION_MATRIX
	}

	private static final Logger log = LoggerFactory.getLogger(UIShader.class);
	private static final String vertexShader = "/shaders/ui/vertex.glsl";
	private static final String fragmentShader = "/shaders/ui/fragment.glsl";
	private int vertexId;
	private int fragmentId;

	public UIShader(GLFWWindow window) {
		super(UIShader.class);
		this.window = window;
	}

	@Override
	public void init() {
		super.init();
		log.debug("Initialising shader...");

		//vertexId = attachVertexShader(vertexShader);
		/*fragmentId = attachFragmentShader(fragmentShader);

		createUniform(vertexId, Uniform.PROJECTION_MATRIX);
		createUniform(fragmentId, Uniform.TEXTURE);*/

		vertexId = GL41.glCreateShader(GL41.GL_VERTEX_SHADER);
		fragmentId = GL41.glCreateShader(GL41.GL_FRAGMENT_SHADER);
		GL41.glShaderSource(vertexId, ResourceLoader.load(vertexShader));
		GL41.glShaderSource(fragmentId, ResourceLoader.load(fragmentShader));
		GL41.glCompileShader(vertexId);
		GL41.glCompileShader(fragmentId);
		if (GL41.glGetShaderi(vertexId, GL41.GL_COMPILE_STATUS) != GL41.GL_TRUE) {
			throw new IllegalStateException();
		}
		if (GL41.glGetShaderi(fragmentId, GL41.GL_COMPILE_STATUS) != GL41.GL_TRUE) {
			throw new IllegalStateException();
		}
		GL41.glAttachShader(programId, vertexId);
		GL41.glAttachShader(programId, fragmentId);
		GL41.glLinkProgram(programId);
		if (GL41.glGetProgrami(programId, GL41.GL_LINK_STATUS) != GL41.GL_TRUE) {
			throw new IllegalStateException();
		}
	}

	@Override
	public void startShader(Material material) {
		//bind();
		GL41.glUseProgram(programId);

		int windowWidth = window.getWindowWidth();
		int windowHeight = window.getWindowHeight();
		int displayWidth = window.getDisplayWidth();
		int displayHeight = window.getDisplayHeight();

		//setUniform(fragmentId, Uniform.TEXTURE, 0);

		/*try (MemoryStack stack = stackPush()) {
			setUniform(vertexId, Uniform.PROJECTION_MATRIX, false, stack.floats(
				2.0f / width, 0.0f, 0.0f, 0.0f,
				0.0f, -2.0f / height, 0.0f, 0.0f,
				0.0f, 0.0f, -1.0f, 0.0f,
				-1.0f, 1.0f, 0.0f, 1.0f
			));
		}*/

		int uniform_tex = GL41.glGetUniformLocation(programId, "TEXTURE");
		int uniform_proj = GL41.glGetUniformLocation(programId, "PROJECTION_MATRIX");

		try (MemoryStack stack = stackPush()) {
			GL41.glUniform1i(uniform_tex, 0);
			GL41.glUniformMatrix4fv(uniform_proj, false, stack.floats(
					2.0f / windowWidth, 0.0f, 0.0f, 0.0f,
					0.0f, -2.0f / windowHeight, 0.0f, 0.0f,
					0.0f, 0.0f, -1.0f, 0.0f,
					-1.0f, 1.0f, 0.0f, 1.0f
			));
		}

		GL41.glViewport(0, 0, displayWidth, displayHeight);
	}

	@Override
	public void stopShader() {
		//unbind();
		GL41.glUseProgram(0);
	}
}
