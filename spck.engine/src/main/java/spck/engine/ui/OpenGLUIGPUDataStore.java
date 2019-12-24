package spck.engine.ui;

import org.lwjgl.nuklear.*;
import org.lwjgl.opengl.GL41;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.framework.GL;
import spck.engine.framework.OpenGLAbstractGPUDataStore;
import spck.engine.util.ResourceLoader;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.MemoryUtil.memAddress;

public class OpenGLUIGPUDataStore extends OpenGLAbstractGPUDataStore {
	private enum LayoutQualifier {
		VX_POSITION(0),
		TEXTURE_COORDS(1),
		COLOR(2);
		public int location;
		LayoutQualifier(int location) {
			this.location = location;
		}
	}

	private static final Logger log = LoggerFactory.getLogger(OpenGLUIGPUDataStore.class);
	private static final int BUFFER_INITIAL_SIZE = 4 * 1024;
	private final NkContext ctx = NkContext.create();
	private final NkUserFont defaultFont = NkUserFont.create();
	private final NkBuffer cmds = NkBuffer.create();
	private final NkDrawNullTexture nullTexture = NkDrawNullTexture.create();
	private final STBTTFontinfo fontInfo = STBTTFontinfo.create();
	private final NkAllocator ALLOCATOR;
	private final STBTTPackedchar.Buffer fontData=STBTTPackedchar.create(95);
	private final ByteBuffer fontTTF = ResourceLoader.loadToByteBuffer("/fonts/GeosansLight.ttf");
	private int vao;
	private int vbo;
	private int ebo;

	public OpenGLUIGPUDataStore() {
		super(-1);

		ALLOCATOR = NkAllocator
			.create()
			.alloc((handle, old, size) -> nmemAllocChecked(size))
			.mfree((handle, ptr) -> nmemFree(ptr));

		MessageBus.register(LifeCycle.GAME_START.eventID(), this::onStart);
	}
	private void onStart() {
		log.debug("Initializing Nuklear...");
		nk_init(ctx, ALLOCATOR, null);
		setupData();
		initStb();
	}

	public int getVAO() {
		return vao;
	}

	public int getVBO() {
		return vbo;
	}

	public int getEBO() {
		return ebo;
	}

	public NkDrawNullTexture getNullTexture() {
		return nullTexture;
	}

	public NkBuffer getCommands() {
		return cmds;
	}

	public NkContext getContext() {
		return ctx;
	}

	public void setupData() {
		log.debug("Creating GPU datastore...");

		nk_buffer_init(cmds, ALLOCATOR, BUFFER_INITIAL_SIZE);

		GL.genVaoContext(vaoId -> {
			vao = vaoId;
			vbo = createVBO();
			ebo = createVBO();

			GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vbo);
			GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, ebo);

			GL41.glEnableVertexAttribArray(LayoutQualifier.VX_POSITION.location);
			GL41.glEnableVertexAttribArray(LayoutQualifier.TEXTURE_COORDS.location);
			GL41.glEnableVertexAttribArray(LayoutQualifier.COLOR.location);

			GL41.glVertexAttribPointer(LayoutQualifier.VX_POSITION.location, 2, GL41.GL_FLOAT, false, 20, 0);
			GL41.glVertexAttribPointer(LayoutQualifier.TEXTURE_COORDS.location, 2, GL41.GL_FLOAT, false, 20, 8);
			GL41.glVertexAttribPointer(LayoutQualifier.COLOR.location, 4, GL41.GL_UNSIGNED_BYTE, true, 20, 16);

			// null texture setup
			int nullTexID = GL41.glGenTextures();

			nullTexture.texture().id(nullTexID);
			nullTexture.uv().set(0.5f, 0.5f);

			GL41.glBindTexture(GL41.GL_TEXTURE_2D, nullTexID);
			try (MemoryStack stack = stackPush()) {
				GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA8, 1, 1, 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_INT_8_8_8_8_REV, stack.ints(0xFFFFFFFF));
			}
			GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);
			GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);

			GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);
			GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, 0);
			GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, 0);
		}, () -> {});
	}

	private void initStb() {
		log.debug("Creating font...");
		int BITMAP_W = 1024;
		int BITMAP_H = 1024;

		int FONT_HEIGHT = 18;
		int fontTexID = GL41.glGenTextures();

		float scale;
		float descent;

		try (MemoryStack stack = stackPush()) {
			stbtt_InitFont(fontInfo, fontTTF);
			scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);

			IntBuffer d = stack.mallocInt(1);
			stbtt_GetFontVMetrics(fontInfo, null, d, null);
			descent = d.get(0) * scale;

			ByteBuffer bitmap = memAlloc(BITMAP_W * BITMAP_H);

			STBTTPackContext pc = STBTTPackContext.mallocStack(stack);
			stbtt_PackBegin(pc, bitmap, BITMAP_W, BITMAP_H, 0, 1, NULL);
			stbtt_PackSetOversampling(pc, 4, 4);
			stbtt_PackFontRange(pc, fontTTF, 0, FONT_HEIGHT, 32, fontData);
			stbtt_PackEnd(pc);

			// Convert R8 to RGBA8
			ByteBuffer texture = memAlloc(BITMAP_W * BITMAP_H * 4);
			for (int i = 0; i < bitmap.capacity(); i++) {
				texture.putInt((bitmap.get(i) << 24) | 0x00FFFFFF);
			}
			texture.flip();

			GL41.glBindTexture(GL41.GL_TEXTURE_2D, fontTexID);
			GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA8, BITMAP_W, BITMAP_H, 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_INT_8_8_8_8_REV, texture);
			GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_LINEAR);
			GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_LINEAR);

			memFree(texture);
			memFree(bitmap);
		}

		defaultFont
				.width((handle, h, text, len) -> {
					float text_width = 0;
					try (MemoryStack stack = stackPush()) {
						IntBuffer unicode = stack.mallocInt(1);

						int glyph_len = nnk_utf_decode(text, memAddress(unicode), len);
						int text_len = glyph_len;

						if (glyph_len == 0) {
							return 0;
						}

						IntBuffer advance = stack.mallocInt(1);
						while (text_len <= len && glyph_len != 0) {
							if (unicode.get(0) == NK_UTF_INVALID) {
								break;
							}

							/* query currently drawn glyph information */
							stbtt_GetCodepointHMetrics(fontInfo, unicode.get(0), advance, null);
							text_width += advance.get(0) * scale;

							/* offset next glyph */
							glyph_len = nnk_utf_decode(text + text_len, memAddress(unicode), len - text_len);
							text_len += glyph_len;
						}
					}
					return text_width;
				})
				.height(FONT_HEIGHT)
				.query((handle, font_height, glyph, codepoint, next_codepoint) -> {
					try (MemoryStack stack = stackPush()) {
						FloatBuffer x = stack.floats(0.0f);
						FloatBuffer y = stack.floats(0.0f);

						STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);
						IntBuffer advance = stack.mallocInt(1);

						stbtt_GetPackedQuad(fontData, BITMAP_W, BITMAP_H, codepoint - 32, x, y, q, false);
						stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);

						NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);

						ufg.width(q.x1() - q.x0());
						ufg.height(q.y1() - q.y0());
						ufg.offset().set(q.x0(), q.y0() + (FONT_HEIGHT + descent));
						ufg.xadvance(advance.get(0) * scale);
						ufg.uv(0).set(q.s0(), q.t0());
						ufg.uv(1).set(q.s1(), q.t1());
					}
				})
				.texture(it -> it
						.id(fontTexID));

		nk_style_set_font(ctx, defaultFont);
	}
}
