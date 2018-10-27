package spck.engine.framework.ecs;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.opengl.GL41;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.Engine;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.graphics.Antialiasing;
import spck.engine.ui.ecs.UIImageComponent;
import spck.engine.ui.ecs.UITextComponent;
import spck.engine.util.Pixel;
import spck.engine.util.ResourceLoader;
import spck.engine.util.RunOnce;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgCreateFontMem;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgFillPaint;
import static org.lwjgl.nanovg.NanoVG.nvgFontFace;
import static org.lwjgl.nanovg.NanoVG.nvgFontSize;
import static org.lwjgl.nanovg.NanoVG.nvgImagePattern;
import static org.lwjgl.nanovg.NanoVG.nvgRect;
import static org.lwjgl.nanovg.NanoVG.nvgText;
import static org.lwjgl.nanovg.NanoVG.nvgTextAlign;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_ANTIALIAS;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_STENCIL_STROKES;
import static org.lwjgl.nanovg.NanoVGGL3.nvgCreate;
import static org.lwjgl.nanovg.NanoVGGL3.nvglCreateImageFromHandle;
import static org.lwjgl.nanovg.NanoVGGLES2.nvgDelete;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.system.MemoryUtil.NULL;

public class UIRendererSystem extends BaseEntitySystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(UIRendererSystem.class);
    private final Map<String, ByteBuffer> loadedFonts = new HashMap<>();
    private ComponentMapper<UITextComponent> textComponents;
    private ComponentMapper<UIImageComponent> imageComponents;
    private long vg;
    private int lastUsedScreenScaleFactor = Engine.window.getPreferences().getScreenScaleFactor();

    public UIRendererSystem() {
        super(Aspect.one(UITextComponent.class, UIImageComponent.class));

        MessageBus.register(LifeCycle.CLEANUP.eventID(), this::cleanUp);
    }

    @Override
    protected void processSystem() {
        RunOnce.run("UIRendererSystem init", () -> {
            vg = Engine.window.getPreferences().getAntialiasing() != Antialiasing.OFF ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES) : nvgCreate(NVG_STENCIL_STROKES);
            if (vg == NULL) {
                throw new RuntimeException("Could not initialize NanoVG");
            }

            loadFont(Engine.preferences.defaultFont, "/fonts/" + Engine.preferences.defaultFont + ".ttf");
        });

        IntBag actives = subscription.getEntities();
        int[] ids = actives.getData();

        if (actives.size() == 0) {
            return;
        }

        if (Engine.preferences.polygonRenderMode) {
            GL41.glPolygonMode(GL41.GL_FRONT_AND_BACK, GL41.GL_FILL);
        }

        nvgBeginFrame(vg, Engine.window.getPreferences().getWidth(), Engine.window.getPreferences().getHeight(), 1);
        nvgFontFace(vg, Engine.preferences.defaultFont);

        for (int i = 0, s = actives.size(); s > i; i++) {
            if (textComponents.has(ids[i])) {
                renderText(ids[i]);
            } else if (imageComponents.has(ids[i])) {
                renderImage(ids[i]);
            }
        }

        nvgEndFrame(vg);

        if (Engine.preferences.polygonRenderMode) {
            GL41.glPolygonMode(GL41.GL_FRONT_AND_BACK, GL41.GL_LINE);
        }

        restoreGLState();
    }

    private void renderImage(int entityId) {
        UIImageComponent component = imageComponents.get(entityId);

        if (lastUsedScreenScaleFactor != Engine.window.getPreferences().getScreenScaleFactor()) {
            component.updateScreenCoords();
        }

        int imageID = nvglCreateImageFromHandle(vg, component.textureId, Pixel.scaled(component.width), Pixel.scaled(component.height), 0);
        NVGPaint paint = NVGPaint.create();
        nvgImagePattern(vg, component.screenCoords.x, component.screenCoords.y, Pixel.scaled(component.width), Pixel.scaled(component.height), 0, imageID, 1f, paint);
        nvgBeginPath(vg);
        nvgRect(vg, component.screenCoords.x, component.screenCoords.y, Pixel.scaled(component.width), Pixel.scaled(component.height));
        nvgFillPaint(vg, paint);
        nvgFill(vg);
    }

    private void renderText(int entityId) {
        UITextComponent component = textComponents.get(entityId);

        if (lastUsedScreenScaleFactor != Engine.window.getPreferences().getScreenScaleFactor()) {
            component.updateScreenCoords();
        }

        // setting fontFace is a slow operation, so we suppose that all the texts are using the default font
        // it not, we handle it here. Use the current text's font, then set back the default
        if (!component.font.equals(Engine.preferences.defaultFont)) {
            nvgFontFace(vg, component.font);
        }

        nvgFontSize(vg, Pixel.scaled(component.size));
        nvgTextAlign(vg, component.align);
        nvgFillColor(vg, component.color);
        nvgText(vg, component.screenCoords.x, component.screenCoords.y, component.text);

        if (!component.font.equals(Engine.preferences.defaultFont)) {
            nvgFontFace(vg, Engine.preferences.defaultFont);
        }
    }

    private void loadFont(String name, String path) {
        if (loadedFonts.containsKey(name)) {
            LOGGER.debug("Font {} already loaded", name);
            return;
        }

        loadedFonts.put(name, ResourceLoader.loadToByteBuffer(path));

        int font = nvgCreateFontMem(vg, name, loadedFonts.get(name), 0);
        if (font == -1) {
            LOGGER.error("Could not add font [{}] from [{}]", name, path);
            throw new RuntimeException("Could not add font");
        }

        LOGGER.debug("Font {} loaded", name);
    }

    private void cleanUp() {
        if (vg != NULL) {
            nvgDelete(vg);
        }
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
}
