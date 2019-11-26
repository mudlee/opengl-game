package spck.engine.framework;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.Antialiasing;
import spck.engine.Engine;
import spck.engine.Input.Input;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.bus.WindowResizedEvent;

import java.nio.IntBuffer;
import java.util.Objects;
import java.util.Optional;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL41.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenGLWindow {
    public static class Preferences {
        private final boolean vSyncEnabled;
        private final Antialiasing antialiasing;
        private final boolean fullscreenEnabled;
        private final boolean limitFPS;
        private String title = "SPCK";
        private Integer devicePixelRatio;
        private int width = 1280;
        private int height = 720;

        public Preferences(boolean vSyncEnabled, Antialiasing antialiasing, boolean fullscreenEnabled, boolean limitFPS) {
            this.vSyncEnabled = vSyncEnabled;
            this.antialiasing = antialiasing;
            this.fullscreenEnabled = fullscreenEnabled;
            this.limitFPS = limitFPS;
        }

        public float getWindowAspect() {
            return (float) Engine.window.getPreferences().getWidth() / (float) Engine.window.getPreferences().getHeight();
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setDevicePixelRatio(int devicePixelRatio) {
            this.devicePixelRatio = devicePixelRatio;
        }

        public boolean isvSyncEnabled() {
            return vSyncEnabled;
        }

        public Antialiasing getAntialiasing() {
            return antialiasing;
        }

        public boolean isFullscreenEnabled() {
            return fullscreenEnabled;
        }

        public String getTitle() {
            return title;
        }

        public Optional<Integer> getDevicePixelRatio() {
            return Optional.ofNullable(devicePixelRatio);
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isLimitFPS() {
            return limitFPS;
        }

        @Override
        public String toString() {
            return "Preferences{" +
                    "vSyncEnabled=" + vSyncEnabled +
                    ", antialiasing=" + antialiasing +
                    ", fullscreenEnabled=" + fullscreenEnabled +
                    ", title='" + title + '\'' +
                    ", devicePixelRatio=" + devicePixelRatio +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGLWindow.class);
    private GLFWVidMode vidMode;
    private boolean resized;
    private final Preferences preferences;
    private long ID;

    public OpenGLWindow(Preferences preferences) {
        this.preferences = preferences;
        MessageBus.register(LifeCycle.GAME_START.eventID(), this::onStart);
        MessageBus.register(LifeCycle.UPDATE.eventID(), this::onUpdate);
        MessageBus.register(LifeCycle.CLEANUP.eventID(), this::onCleanUp);
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    public boolean shouldNotClose() {
        return !glfwWindowShouldClose(ID);
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(ID);
    }

    public long getID() {
        return ID;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    private void onStart() {
        LOGGER.debug("Initialising window...");
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        if (!glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (preferences.antialiasing != Antialiasing.OFF) {
            glfwWindowHint(GLFW_SAMPLES, preferences.antialiasing.getValue());
        }

        if (preferences.fullscreenEnabled) {
            prepareFullscreen();
        }

        // creating window
        LOGGER.debug("Creating window with width {} height {}", preferences.getWidth(), preferences.getHeight());
        ID = glfwCreateWindow(preferences.getWidth(), preferences.getHeight(), preferences.getTitle(), (preferences.fullscreenEnabled) ? glfwGetPrimaryMonitor() : NULL, NULL);
        if (ID == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(ID);

        // watch for resize
        final WindowResizedEvent windowResizedEvent = new WindowResizedEvent();
        glfwSetFramebufferSizeCallback(ID, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                preferences.setWidth(width);
                preferences.setHeight(height);
                resized = true;

                windowResizedEvent.set(width, height);
                MessageBus.broadcast(LifeCycle.WINDOW_RESIZED.eventID(), windowResizedEvent);
            }
        });

        // INPUT HANDLING
        glfwSetKeyCallback(ID, Input.keyCallback());
        glfwSetCursorPosCallback(ID, Input.mouseCursorCallback());
        glfwSetScrollCallback(ID, Input.mouseScrollCallback());
        glfwSetMouseButtonCallback(ID, Input.mouseButtonCallback());

        // VSYNC
        if (preferences.vSyncEnabled) {
            glfwSwapInterval(1);
        } else {
            glfwSwapInterval(0);
        }

        // windowed mode setup
        if (!preferences.fullscreenEnabled) {
            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            if (vidmode == null) {
                throw new RuntimeException("Could not get vidmode");
            }

            // Center our window
            glfwSetWindowPos(
                    ID,
                    (vidmode.width() - preferences.getWidth()) / 2,
                    (vidmode.height() - preferences.getHeight()) / 2
            );
        }

        glfwShowWindow(ID);
        calculateScreenScaleFactor();
        GLCapabilities capabilities = createCapabilities();
        if (capabilities.OpenGL43) {
            LOGGER.debug("OpenGL 4.3 debugging enabled");
            GL43.glDebugMessageControl(
                    GL43.GL_DEBUG_SOURCE_API, GL43.GL_DEBUG_TYPE_OTHER,
                    GL43.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer) null, false
            );
        } else if (capabilities.GL_ARB_debug_output) {
            LOGGER.debug("ARB debugging enabled");
            ARBDebugOutput.glDebugMessageControlARB(
                    ARBDebugOutput.GL_DEBUG_SOURCE_API_ARB,
                    ARBDebugOutput.GL_DEBUG_TYPE_OTHER_ARB, ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB, (IntBuffer) null,
                    false
            );
        }

        LOGGER.debug("OpenGL Vendor: {}", glGetString(GL_VENDOR));
        LOGGER.debug("Version: {}", glGetString(GL_VERSION));
        LOGGER.debug("Renderer: {}", glGetString(GL_RENDERER));
        LOGGER.debug("Shading Language Version: {}", glGetString(GL_SHADING_LANGUAGE_VERSION));

        // window resize setup
        preferences.setWidth((preferences.fullscreenEnabled ? vidMode.width() : preferences.getWidth()));
        preferences.setHeight((preferences.fullscreenEnabled ? vidMode.height() : preferences.getHeight()));

        // misc
        // NOTE: UI might touch the state, use UI.restoreGLState to restore state
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        LOGGER.debug("Window has been intialised");

        Input.onKeyPressed(GLFW_KEY_Q, event -> glfwSetWindowShouldClose(ID, true));
    }

    public void captureMouse() {
        glfwSetInputMode(ID, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void releaseMouse() {
        glfwSetInputMode(ID, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    private void onUpdate() {
        if (resized) {
            glViewport(0, 0, preferences.getWidth(), preferences.getHeight());
            resized = false;
            LOGGER.debug("Window resized to {}x{}, scale factor {}", preferences.getWidth(), preferences.getHeight(), preferences.getDevicePixelRatio());
        }
    }

    private void onCleanUp() {
        LOGGER.debug("Cleaning up...");
        glfwFreeCallbacks(ID);
        glfwDestroyWindow(ID);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void prepareFullscreen() {
        // Set up a fixed width and height so window initialization does not fail
        LOGGER.debug("{}-{}", vidMode.width(), vidMode.height());
        preferences.setWidth(vidMode.width());
        preferences.setHeight(vidMode.height());
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
    }

    private void calculateScreenScaleFactor() {
        // https://en.wikipedia.org/wiki/4K_resolution
        int uhdMinWidth = 3840;
        int uhdMinHeight = 1716;
        boolean UHD = vidMode.width() >= uhdMinWidth && vidMode.height() >= uhdMinHeight;
        LOGGER.debug("Screen is {}x{}, UHD: {}", vidMode.width(), vidMode.height(), UHD);

        // Check if the monitor is 4K
        if (UHD) {
            preferences.setDevicePixelRatio(2);
            LOGGER.debug("Device pixel ratio has been set to: {}", preferences.getDevicePixelRatio().get());
            return;
        }

        IntBuffer widthScreenCoordBuf = MemoryUtil.memAllocInt(1);
        IntBuffer heightScreenCoordBuf = MemoryUtil.memAllocInt(1);
        IntBuffer widthPixelsBuf = MemoryUtil.memAllocInt(1);
        IntBuffer heightPixelsBuf = MemoryUtil.memAllocInt(1);

        glfwGetWindowSize(ID, widthScreenCoordBuf, heightScreenCoordBuf);
        glfwGetFramebufferSize(ID, widthPixelsBuf, heightPixelsBuf);

        preferences.setDevicePixelRatio((int) Math.floor(widthPixelsBuf.get() / widthScreenCoordBuf.get()));
        LOGGER.debug("Device pixel ratio has been set to: {}", preferences.getDevicePixelRatio().get());

        MemoryUtil.memFree(widthScreenCoordBuf);
        MemoryUtil.memFree(heightScreenCoordBuf);
        MemoryUtil.memFree(widthPixelsBuf);
        MemoryUtil.memFree(heightPixelsBuf);
    }
}
