package spck.engine.framework;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.KeyEvent;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.bus.WindowResizedEvent;
import spck.engine.graphics.Antialiasing;

import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.opengl.GL41.GL_BACK;
import static org.lwjgl.opengl.GL41.GL_BLEND;
import static org.lwjgl.opengl.GL41.GL_CULL_FACE;
import static org.lwjgl.opengl.GL41.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL41.GL_FALSE;
import static org.lwjgl.opengl.GL41.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL41.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL41.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL41.GL_TRUE;
import static org.lwjgl.opengl.GL41.GL_VERSION;
import static org.lwjgl.opengl.GL41.glBlendFunc;
import static org.lwjgl.opengl.GL41.glCullFace;
import static org.lwjgl.opengl.GL41.glEnable;
import static org.lwjgl.opengl.GL41.glGetString;
import static org.lwjgl.opengl.GL41.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLFWOpenGLWindow {
    public static class Preferences {
        private final boolean vSyncEnabled;
        private final Antialiasing antialiasing;
        private final boolean fullscreenEnabled;
        private String title = "SPCK";
        private int screenScaleFactor = 1;
        private int width = 1280;
        private int height = 720;

        public Preferences(boolean vSyncEnabled, Antialiasing antialiasing, boolean fullscreenEnabled) {
            this.vSyncEnabled = vSyncEnabled;
            this.antialiasing = antialiasing;
            this.fullscreenEnabled = fullscreenEnabled;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setScreenScaleFactor(int screenScaleFactor) {
            this.screenScaleFactor = screenScaleFactor;
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

        public int getScreenScaleFactor() {
            return screenScaleFactor;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GLFWOpenGLWindow.class);
    private GLFWVidMode vidMode;
    private boolean resized;
    private final Preferences preferences;
    private long ID;

    public GLFWOpenGLWindow(Preferences preferences) {
        this.preferences = preferences;
        MessageBus.register(LifeCycle.GAME_START.eventID(), this::onStart);
        MessageBus.register(LifeCycle.UPDATE.eventID(), this::onUpdate);
        MessageBus.register(LifeCycle.CLEANUP.eventID(), this::onCleanUp);
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

        if (!glfwVulkanSupported()) {
            throw new RuntimeException("GLFW failed to find the Vulkan loader");
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
        glfwSetFramebufferSizeCallback(ID, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                preferences.setWidth(width);
                preferences.setHeight(height);
                resized = true;

                WindowResizedEvent.reusable.width = width;
                WindowResizedEvent.reusable.height = height;
                MessageBus.broadcast(LifeCycle.WINDOW_RESIZED.eventID(), WindowResizedEvent.reusable);
            }
        });

        // initializing inputs
        final KeyEvent keyEvent = new KeyEvent();
        glfwSetKeyCallback(ID, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                keyEvent.set(key, scancode, action, mods);

                if (action == GLFW_PRESS) {
                    MessageBus.broadcast(KeyEvent.pressed(key), keyEvent);
                } else if (action == GLFW_RELEASE) {
                    MessageBus.broadcast(KeyEvent.released(key), keyEvent);
                }
            }
        });


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
        GL.createCapabilities();

        // window resize setup
        preferences.setWidth((preferences.fullscreenEnabled ? vidMode.width() : preferences.getWidth()) * preferences.getScreenScaleFactor());
        preferences.setHeight((preferences.fullscreenEnabled ? vidMode.height() : preferences.getHeight()) * preferences.getScreenScaleFactor());

        // misc
        // NOTE: UI might touch the state, use UI.restoreGLState to restore state
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        LOGGER.debug("Window has been intialised");
        LOGGER.debug("OpenGL version " + glGetString(GL_VERSION));

        MessageBus.register(KeyEvent.pressed(GLFW_KEY_Q), (event) -> {
            glfwSetWindowShouldClose(ID, true);
        });
    }

    private void onUpdate() {
        if (resized) {
            glViewport(0, 0, preferences.getWidth(), preferences.getHeight());
            resized = false;
            LOGGER.debug("Window resized to {}x{}, scale factor {}", preferences.getWidth(), preferences.getHeight(), preferences.getScreenScaleFactor());
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
        IntBuffer widthScreenCoordBuf = MemoryUtil.memAllocInt(1);
        IntBuffer heightScreenCoordBuf = MemoryUtil.memAllocInt(1);
        IntBuffer widthPixelsBuf = MemoryUtil.memAllocInt(1);
        IntBuffer heightPixelsBuf = MemoryUtil.memAllocInt(1);

        glfwGetWindowSize(ID, widthScreenCoordBuf, heightScreenCoordBuf);
        glfwGetFramebufferSize(ID, widthPixelsBuf, heightPixelsBuf);

        preferences.setScreenScaleFactor((int) Math.floor(widthPixelsBuf.get() / widthScreenCoordBuf.get()));
        LOGGER.debug("Screen scale factor has been set to: {}", preferences.getScreenScaleFactor());

        MemoryUtil.memFree(widthScreenCoordBuf);
        MemoryUtil.memFree(heightScreenCoordBuf);
        MemoryUtil.memFree(widthPixelsBuf);
        MemoryUtil.memFree(heightPixelsBuf);
    }
}
