package spck.engine.ui;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import org.joml.Vector2d;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.opengl.GL41;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.Engine;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.framework.Graphics;
import spck.engine.framework.RGBAColor;
import spck.engine.util.ResourceLoader;
import spck.engine.window.Antialiasing;
import spck.engine.window.GLFWWindow;
import spck.engine.window.Input;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class UIRendererSystem extends BaseEntitySystem {
	private static final Logger log = LoggerFactory.getLogger(UIRendererSystem.class);
	private final Vector2d MOUSE_POS_TEMP = new Vector2d();
	private final String defaultFont;
	private final Input input;
	private final Map<String, ByteBuffer> loadedFonts = new HashMap<>();
	private final GLFWWindow window;
	private ComponentMapper<CanvasComponent> canvasComponents;
	private Long pointer;
	private boolean leftMouseButtonClicked;

	public UIRendererSystem(
			String defaultFont,
			GLFWWindow window,
			Input input
	) {

		super(Aspect.one(CanvasComponent.class));
		this.window = window;
		this.defaultFont = defaultFont;
		this.input = input;

		MessageBus.register(LifeCycle.GAME_START.eventID(), this::onStart);
		MessageBus.register(LifeCycle.CLEANUP.eventID(), this::onCleanUp);
		input.onMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT, event -> leftMouseButtonClicked = true);
		input.onMouseButtonReleased(GLFW_MOUSE_BUTTON_LEFT, event -> leftMouseButtonClicked = false);
	}

	@Override
	protected void processSystem() {
		IntBag actives = subscription.getEntities();
		int[] ids = actives.getData();

		if (actives.size() == 0) {
			return;
		}

		if (Engine.preferences.polygonRenderMode) Graphics.setPolygonMode(Graphics.PolygonMode.FILL);

		render(actives, ids);

		if (Engine.preferences.polygonRenderMode) Graphics.setPolygonMode(Graphics.PolygonMode.LINE);
	}

	private void onStart() {
		pointer = window.getAntialiasing() != Antialiasing.OFF ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES) : nvgCreate(NVG_STENCIL_STROKES);
		if (pointer == NULL) {
			throw new RuntimeException("Could not initialize NanoVG");
		}

		loadFont(defaultFont, "/fonts/" + defaultFont + ".ttf");
	}

	private void onCleanUp() {
		log.debug("Cleaning up...");
		nvgDelete(pointer);
	}

	private void render(IntBag actives, int[] ids) {
		MOUSE_POS_TEMP.set(input.getMouseRelativePosition());
		nvgBeginFrame(pointer, window.getWindowWidth(), window.getWindowHeight(), window.getDevicePixelRatio());
		nvgFontFace(pointer, defaultFont);

		for (int i = 0, s = actives.size(); s > i; i++) {
			if (canvasComponents.has(ids[i])) {
				CanvasComponent canvas = canvasComponents.get(ids[i]);
				for (Text text : canvas.texts) {
					renderText(text);
				}

				for (Image image : canvas.images) {
					renderImage(image);
				}

				for (Button button : canvas.buttons) {
					renderbutton(button);
				}
			}
		}

		nvgEndFrame(pointer);
		restoreGLState();
	}

	// TODO: check which part does what
	// TODO: align right and center are wrong
	// TODO: z-index
	private void renderbutton(Button button) {
		NVGPaint bg = NVGPaint.create();

		int width = button.width * window.getDevicePixelRatio();
		int height = button.height * window.getDevicePixelRatio();
		int textSize = button.textSize * window.getDevicePixelRatio();
		int cornerRadius = button.cornerRadius * window.getDevicePixelRatio();
		// TODO: cache positions
		float x = calculatePosX(button.x, button.align);
		float y = calculatePosY(button.y, button.align);

		boolean mouseOver = button.backgroundMouseOverColor != null && isMouseOverButton((int) x, (int) y, width, height);

		if (leftMouseButtonClicked && mouseOver && button.onClickHandler != null) {
			button.onClickHandler.run();
		}

		RGBAColor bgColor = mouseOver ? button.backgroundMouseOverColor : button.backgroundColor;

		nvgLinearGradient(
				pointer,
				x,
				y,
				x,
				y + height,
				bgColor.toNVGColor(),
				bgColor.toNVGColor(),
				bg
		);

		nvgBeginPath(pointer);
		nvgRoundedRect(pointer, x + 1, y + 1, width - 2, height - 2, cornerRadius - 1);
		nvgFillPaint(pointer, bg);
		nvgFill(pointer);

		if(button.stroke != null) {
			nvgBeginPath(pointer);
			nvgRoundedRect(pointer, x + 0.5f, y + 0.5f, width - 1, height - 1, cornerRadius - 0.5f);
			nvgStrokeColor(pointer, button.stroke.color.toNVGColor());
			nvgStroke(pointer);
		}

		if (!button.textFont.equals(defaultFont)) {
			nvgFontFace(pointer, button.textFont);
		}

		nvgFontSize(pointer, textSize);
		float textWidth = nvgTextBounds(pointer, 0, 0, button.text, (FloatBuffer) null);

		nvgFontSize(pointer, textSize);
		nvgTextAlign(pointer, button.textAlign.getNvgValue());
		nvgFillColor(pointer, button.textColor.toNVGColor());
		nvgText(pointer, x + width * 0.5f - textWidth * 0.5f * 0.25f, y + height * 0.5f - 1, button.text);

		if (!button.textFont.equals(defaultFont)) {
			nvgFontFace(pointer, defaultFont);
		}
	}

	private void renderText(Text text) {
		// setting fontFace is a slow operation, so we suppose that all the texts are using the default font
		// if not, we handle it here. Use the current text's font, then set back the default
		if (!text.getFont().equals(defaultFont)) {
			nvgFontFace(pointer, text.getFont());
		}

		// TODO: cache positions
		float x = calculatePosX(text.x, text.align);
		float y = calculatePosY(text.y, text.align);

		nvgFontSize(pointer, text.getSize() * window.getDevicePixelRatio());
		nvgTextAlign(pointer, text.getAlign().getNvgValue());
		nvgFillColor(pointer, text.getColor().toNVGColor());
		nvgText(pointer, x, y, text.getText());

		if (!text.getFont().equals(defaultFont)) {
			nvgFontFace(pointer, defaultFont);
		}
	}

	private void renderImage(Image image) {
		int width = image.getWidth() * window.getDevicePixelRatio();
		int height = image.getHeight() * window.getDevicePixelRatio();

		// TODO: cache positions
		float x = calculatePosX(image.x, image.align);
		float y = calculatePosY(image.y, image.align);

		if (image.handle == null) {
			image.handle = nvglCreateImageFromHandle(pointer, image.getTextureId(), width, height, 0);
		}

		NVGPaint paint = NVGPaint.create();
		nvgImagePattern(pointer, x, y, width, height, 0, image.handle, 1f, paint);
		nvgBeginPath(pointer);
		nvgRect(pointer, x, y, width, height);
		nvgFillPaint(pointer, paint);
		nvgFill(pointer);
	}

	private boolean isMouseOverButton(int xMin, int yMin, int width, int height) {
		int xMax = xMin + width;
		int yMax = yMin + height;

		return (int) MOUSE_POS_TEMP.x >= xMin &&
				(int) MOUSE_POS_TEMP.x <= xMax &&
				(int) MOUSE_POS_TEMP.y >= yMin &&
				(int) MOUSE_POS_TEMP.y <= yMax;
	}

	private float calculatePosX(int x, Align align) {
		switch (align) {
			case TOP_LEFT:
			case BOTTOM_LEFT:
			case MIDDLE_LEFT:
				return x;
			case TOP_RIGHT:
			case BOTTOM_RIGHT:
			case MIDDLE_RIGHT:
				return window.getWindowWidth() - x;
			case TOP_CENTER:
			case MIDDLE_CENTER:
			case BOTTOM_CENTER:
				return window.getWindowWidth() / 2f;
		}

		throw new RuntimeException("Not supported align: " + align);
	}

	private float calculatePosY(int y, Align align) {
		switch (align) {
			case TOP_LEFT:
			case TOP_RIGHT:
			case TOP_CENTER:
				return y;
			case BOTTOM_LEFT:
			case BOTTOM_RIGHT:
			case BOTTOM_CENTER:
				return window.getWindowHeight() - y;
			case MIDDLE_LEFT:
			case MIDDLE_RIGHT:
			case MIDDLE_CENTER:
				return window.getWindowHeight() / 2f;
		}

		throw new RuntimeException("Not supported align: " + align);
	}

	private void loadFont(String name, String path) {
		if (loadedFonts.containsKey(name)) {
			log.debug("Font {} already loaded", name);
			return;
		}

		loadedFonts.put(name, ResourceLoader.loadToByteBuffer(path));

		int font = nvgCreateFontMem(pointer, name, loadedFonts.get(name), 0);
		if (font == -1) {
			log.error("Could not add font [{}] from [{}]", name, path);
			throw new RuntimeException("Could not add font");
		}

		log.debug("Font {} loaded", name);
	}

	private void restoreGLState() {
		GL41.glEnable(GL41.GL_DEPTH_TEST);
		GL41.glEnable(GL41.GL_STENCIL_TEST);
		GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);
		GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, 0);
		GL41.glBindBuffer(GL41.GL_UNIFORM_BUFFER, 0);
		GL41.glEnable(GL41.GL_BLEND);
		GL41.glBlendFunc(GL41.GL_SRC_ALPHA, GL41.GL_ONE_MINUS_SRC_ALPHA);
		GL41.glEnable(GL41.GL_CULL_FACE);
		GL41.glCullFace(GL41.GL_BACK);
		GL41.glBindVertexArray(0);
	}
}
