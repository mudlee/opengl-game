package spck.engine.window;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.bus.WindowResizedEvent;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLFWWindow {
	private static final Logger log = LoggerFactory.getLogger(GLFWWindow.class);
	private final GLFWPreferences preferences;
	private final Input input;
	private final DoubleBuffer mouseCursorAbsolutePositionX = MemoryUtil.memAllocDouble(1);
	private final DoubleBuffer mouseCursorAbsolutePositionY = MemoryUtil.memAllocDouble(1);
	private final Vector2f windowScale = new Vector2f();
	private int windowWidth;
	private int windowHeight;
	private int displayWidth;
	private int displayHeight;
	private boolean vSync;
	private boolean limitFps;
	private Antialiasing antialiasing;
	private int devicePixelRatio;
	private long id;
	private GLFWVidMode vidMode;
	private boolean wasResized;

	public GLFWWindow(GLFWPreferences preferences, Input input) {
		this.preferences = preferences;
		this.windowWidth = preferences.getWidth();
		this.windowHeight = preferences.getHeight();
		this.vSync = preferences.isVsync();
		this.limitFps = preferences.isLimitFps();
		this.antialiasing = preferences.getAntialiasing();
		this.input = input;
	}

	public void create() {
		log.debug("Initialising window with preferences {}", preferences);

		MessageBus.register(LifeCycle.UPDATE.eventID(), this::onUpdate);
		MessageBus.register(LifeCycle.CLEANUP.eventID(), this::onCleanUp);

		glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

		if (!glfwInit()) {
			throw new RuntimeException("Unable to initialize GLFW");
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GL41.GL_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GL41.GL_TRUE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL41.GL_TRUE);

		vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

		if (preferences.getAntialiasing() != Antialiasing.OFF) {
			glfwWindowHint(GLFW_SAMPLES, preferences.getAntialiasing().getValue());
		}

		// fullscreen if needed
		if (preferences.isFullscreen()) {
			windowWidth = vidMode.width();
			windowHeight = vidMode.height();
			glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
		}

		log.debug("Creating window with width {} height {}", preferences.getWidth(), preferences.getHeight());
		id = glfwCreateWindow(
				preferences.getWidth(),
				preferences.getHeight(),
				preferences.getTitle(),
				preferences.isFullscreen() ? glfwGetPrimaryMonitor() : NULL, NULL
		);
		if (id == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

		glfwMakeContextCurrent(id);
		watchForResize();

		// INPUT HANDLING
		glfwSetKeyCallback(id, (window, key, scancode, action, mods) -> input.keyCallback(key, scancode, action, mods));
		glfwSetCursorPosCallback(id, (window, x, y) -> input.cursorPosCallback(x, y));
		glfwSetScrollCallback(id, (window, xOffset, yOffset) -> input.mouseScrollCallback(xOffset, yOffset));
		glfwSetMouseButtonCallback(id, (window, button, action, mods) -> input.mouseButtonCallback(button, action, mods));

		// V-SYNC
		glfwSwapInterval(preferences.isVsync() ? 1 : 0);

		centerWindow();

		glfwShowWindow(id);

		// DPI scale
		try (MemoryStack stack = stackPush()) {
			FloatBuffer sx = stack.mallocFloat(1);
			FloatBuffer sy = stack.mallocFloat(1);
			glfwGetWindowContentScale(id, sx, sy);
			windowScale.set(sx.get(), sy.get());
			log.debug("Window scale was set to x:{} y:{}", windowScale.x, windowScale.y);
		}
		glfwSetWindowContentScaleCallback(id, (window, xScale, yScale) -> {
			windowScale.set(xScale, yScale);
			log.debug("Window scale was set to x:{} y:{}", windowScale.x, windowScale.y);
		});

		calculateDevicePixelRatio();
		setDisplayDimensions();

		setupOpenGLDebug();

		log.debug("OpenGL Vendor: {}", GL41.glGetString(GL41.GL_VENDOR));
		log.debug("Version: {}", GL41.glGetString(GL41.GL_VERSION));
		log.debug("Renderer: {}", GL41.glGetString(GL41.GL_RENDERER));
		log.debug("Shading Language Version: {}", GL41.glGetString(GL41.GL_SHADING_LANGUAGE_VERSION));

		// misc
		// NOTE: UI might touch the state, use UI.restoreGLState to restore state
		GL41.glEnable(GL41.GL_DEPTH_TEST);
		GL41.glEnable(GL41.GL_STENCIL_TEST);
		GL41.glEnable(GL41.GL_BLEND);
		GL41.glBlendFunc(GL41.GL_SRC_ALPHA, GL41.GL_ONE_MINUS_SRC_ALPHA);
		GL41.glEnable(GL41.GL_CULL_FACE);
		GL41.glCullFace(GL41.GL_BACK);

		log.debug("Window has been intialised");

		input.onKeyPressed(GLFW_KEY_Q, event -> glfwSetWindowShouldClose(id, true));
	}

	private void calculateDevicePixelRatio() {
		// https://en.wikipedia.org/wiki/4K_resolution
		int uhdMinWidth = 3840;
		int uhdMinHeight = 1716;
		boolean UHD = vidMode.width() >= uhdMinWidth && vidMode.height() >= uhdMinHeight;
		log.debug("Screen is {}x{}, UHD: {}", vidMode.width(), vidMode.height(), UHD);

		// Check if the monitor is 4K
		if (UHD) {
			devicePixelRatio = 2;
			log.debug("Device pixel ratio has been set to: {}", devicePixelRatio);
			return;
		}

		IntBuffer widthScreenCoordBuf = MemoryUtil.memAllocInt(1);
		IntBuffer heightScreenCoordBuf = MemoryUtil.memAllocInt(1);
		IntBuffer widthPixelsBuf = MemoryUtil.memAllocInt(1);
		IntBuffer heightPixelsBuf = MemoryUtil.memAllocInt(1);

		glfwGetWindowSize(id, widthScreenCoordBuf, heightScreenCoordBuf);
		glfwGetFramebufferSize(id, widthPixelsBuf, heightPixelsBuf);

		devicePixelRatio = (int) Math.floor((float)widthPixelsBuf.get() / (float)widthScreenCoordBuf.get());
		log.debug("Device pixel ratio has been set to: {}", devicePixelRatio);

		MemoryUtil.memFree(widthScreenCoordBuf);
		MemoryUtil.memFree(heightScreenCoordBuf);
		MemoryUtil.memFree(widthPixelsBuf);
		MemoryUtil.memFree(heightPixelsBuf);
	}

	public Consumer<Vector2d> getCursorPositionConsumer() {
		return target -> {
			mouseCursorAbsolutePositionX.clear();
			mouseCursorAbsolutePositionY.clear();
			glfwGetCursorPos(id, mouseCursorAbsolutePositionX, mouseCursorAbsolutePositionY);
			target.set(mouseCursorAbsolutePositionX.get(), mouseCursorAbsolutePositionY.get());
		};
	}

	public float getWindowAspect() {
		return (float) windowWidth / (float) windowHeight;
	}

	public void captureMouse() {
		glfwSetInputMode(id, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
	}

	public void setMousePosition(Vector2d position) {
		glfwSetCursorPos(id, position.x, position.y);
	}

	public void releaseMouse() {
		glfwSetInputMode(id, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
	}

	public boolean shouldNotClose() {
		return !glfwWindowShouldClose(id);
	}

	public void swapBuffers() {
		glfwSwapBuffers(id);
	}

	public void pollEvents() {
		glfwPollEvents();
	}

	public Vector2f getWindowScale() {
		return windowScale;
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public int getDisplayWidth() {
		return displayWidth;
	}

	public int getDisplayHeight() {
		return displayHeight;
	}

	public boolean isvSync() {
		return vSync;
	}

	public boolean isLimitFps() {
		return limitFps;
	}

	public Antialiasing getAntialiasing() {
		return antialiasing;
	}

	public int getDevicePixelRatio() {
		return devicePixelRatio;
	}

	public long getId() {
		return id;
	}

	private void onUpdate() {
		if (wasResized) {
			glViewport(0, 0, windowWidth, windowHeight);
			calculateDevicePixelRatio();
			wasResized = false;
			log.debug("Window resized to {}x{}", windowWidth, windowHeight);
		}
	}

	private void onCleanUp() {
		log.debug("Cleaning up...");
		MemoryUtil.memFree(mouseCursorAbsolutePositionX);
		MemoryUtil.memFree(mouseCursorAbsolutePositionY);
		glfwFreeCallbacks(id);
		glfwDestroyWindow(id);
		glfwTerminate();
		Objects.requireNonNull(glfwSetErrorCallback(null)).free();
	}

	private void setupOpenGLDebug() {
		GLCapabilities capabilities = createCapabilities();
		if (capabilities.OpenGL43) {
			log.debug("OpenGL 4.3 debugging enabled");
			GL43.glDebugMessageControl(
				GL43.GL_DEBUG_SOURCE_API,
				GL43.GL_DEBUG_TYPE_OTHER,
				GL43.GL_DEBUG_SEVERITY_NOTIFICATION,
				(IntBuffer) null,
				false
			);
		}
		else if(capabilities.GL_KHR_debug){
			KHRDebug.glDebugMessageControl(
				KHRDebug.GL_DEBUG_SOURCE_API,
				KHRDebug.GL_DEBUG_TYPE_OTHER,
				KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
				(IntBuffer)null,
				false
			);
		}
		else if (capabilities.GL_ARB_debug_output) {
			log.debug("ARB debugging enabled");
			ARBDebugOutput.glDebugMessageControlARB(
					ARBDebugOutput.GL_DEBUG_SOURCE_API_ARB,
					ARBDebugOutput.GL_DEBUG_TYPE_OTHER_ARB,
					ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB,
					(IntBuffer) null,
					false
			);
		}
	}

	private void centerWindow() {
		if (!preferences.isFullscreen()) {
			// Center our window
			glfwSetWindowPos(
					id,
					(vidMode.width() - windowWidth) / 2,
					(vidMode.height() - windowHeight) / 2
			);
		}
	}

	private void watchForResize() {
		// watch for resize
		final WindowResizedEvent windowResizedEvent = new WindowResizedEvent();
		glfwSetWindowSizeCallback(id, (window, width, height) -> {
			windowWidth = width;
			windowHeight = height;
			wasResized = true;

			windowResizedEvent.set(windowWidth, windowHeight);
			input.windowResizedCallback(windowWidth, windowHeight);
			MessageBus.broadcast(LifeCycle.WINDOW_RESIZED.eventID(), windowResizedEvent);
		});
		glfwSetFramebufferSizeCallback(id, (window, width, height) -> {
			displayWidth = width;
			displayHeight = height;
		});
	}

	private void setDisplayDimensions() {
		try (MemoryStack stack = stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);

			glfwGetFramebufferSize(id, w, h);
			displayWidth = w.get(0);
			displayHeight = h.get(0);
		}
	}
}
