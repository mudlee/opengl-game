package spck.engine.framework;

import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.opengl.GL41;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.ecs.ui.UIImage;
import spck.engine.ecs.ui.UIText;
import spck.engine.util.ResourceLoader;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.nanovg.NanoVGGLES2.nvgDelete;
import static org.lwjgl.opengl.GL41.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class UIRenderer {
    public enum Align {
        LEFT(NVG_ALIGN_LEFT),
        TOP(NVG_ALIGN_TOP),
        RIGHT(NVG_ALIGN_RIGHT),
        BOTTOM(NVG_ALIGN_BOTTOM);

        private int nvg;

        Align(int nvg) {
            this.nvg = nvg;
        }

        public int getValue() {
            return nvg;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UIRenderer.class);
    private final String defaultFont;
    private final Map<String, ByteBuffer> loadedFonts = new HashMap<>();
    private Long pointer;

    public UIRenderer(String defaultFont) {
        this.defaultFont = defaultFont;
    }

    public void init(boolean antialiasingEnabled) {
        pointer = antialiasingEnabled ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES) : nvgCreate(NVG_STENCIL_STROKES);
        if (pointer == NULL) {
            throw new RuntimeException("Could not initialize NanoVG");
        }

        loadFont(defaultFont, "/fonts/" + defaultFont + ".ttf");

        MessageBus.register(LifeCycle.CLEANUP.eventID(), this::cleanUp);
    }

    public void beginFrame(int screenWidth, int screenHeight, int devicePixelRatio) {
        nvgBeginFrame(pointer, screenWidth, screenHeight, devicePixelRatio);
        nvgFontFace(pointer, defaultFont);
    }

    public void endFrame() {
        nvgEndFrame(pointer);
        restoreGLState();
    }

    public void renderImage(UIImage image) {
        checkIfInitialised();

        int width = image.getWidth();
        int height = image.getHeight();
        float x = image.getScreenCoords().x;
        float y = image.getScreenCoords().y;

        if (image.handle == null) {
            image.handle = nvglCreateImageFromHandle(pointer, image.getTextureId(), width, height, 0);
        }

        //int imageID = nvglCreateImageFromHandle(pointer, image.getTextureId(), width, height, 0);
        NVGPaint paint = NVGPaint.create();
        nvgImagePattern(pointer, x, y, width, height, 0, image.handle, 1f, paint);
        nvgBeginPath(pointer);
        nvgRect(pointer, x, y, width, height);
        nvgFillPaint(pointer, paint);
        nvgFill(pointer);
    }

    public void renderText(UIText text) {
        checkIfInitialised();

        // setting fontFace is a slow operation, so we suppose that all the texts are using the default font
        // if not, we handle it here. Use the current text's font, then set back the default
        if (!text.getFont().equals(defaultFont)) {
            nvgFontFace(pointer, text.getFont());
        }

        int textSize = text.getSize();
        float x = text.getScreenCoords().x;
        float y = text.getScreenCoords().y;

        nvgFontSize(pointer, textSize);
        nvgTextAlign(pointer, text.getAlign());
        nvgFillColor(pointer, text.getColor().getColor());
        nvgText(pointer, x, y, text.getText());

        if (!text.getFont().equals(defaultFont)) {
            nvgFontFace(pointer, defaultFont);
        }
    }

    private void checkIfInitialised() {
        if (pointer == null) {
            throw new RuntimeException("UI rendering is not initialised");
        }
    }

    private void cleanUp() {
        LOGGER.debug("Cleaning up...");
        nvgDelete(pointer);
    }

    private void restoreGLState() {
        GL41.glEnable(GL41.GL_DEPTH_TEST);
        GL41.glEnable(GL41.GL_STENCIL_TEST);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        GL41.glEnable(GL41.GL_BLEND);
        GL41.glBlendFunc(GL41.GL_SRC_ALPHA, GL41.GL_ONE_MINUS_SRC_ALPHA);
        GL41.glEnable(GL41.GL_CULL_FACE);
        GL41.glCullFace(GL41.GL_BACK);
        glBindVertexArray(0);
    }

    private void loadFont(String name, String path) {
        if (loadedFonts.containsKey(name)) {
            LOGGER.debug("Font {} already loaded", name);
            return;
        }

        loadedFonts.put(name, ResourceLoader.loadToByteBuffer(path));

        int font = nvgCreateFontMem(pointer, name, loadedFonts.get(name), 0);
        if (font == -1) {
            LOGGER.error("Could not add font [{}] from [{}]", name, path);
            throw new RuntimeException("Could not add font");
        }

        LOGGER.debug("Font {} loaded", name);
    }
}
