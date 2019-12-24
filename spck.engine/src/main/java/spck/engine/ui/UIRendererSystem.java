package spck.engine.ui;

import com.artemis.BaseSystem;
import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.*;
import org.lwjgl.opengl.GL41;
import org.lwjgl.system.MemoryStack;
import spck.engine.framework.GL;
import spck.engine.util.RunOnce;
import spck.engine.window.GLFWWindow;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class UIRendererSystem extends BaseSystem {
	private static final int MAX_VERTEX_BUFFER = 512 * 1024;
	private static final int MAX_ELEMENT_BUFFER = 128 * 1024;
	private final NkDrawVertexLayoutElement.Buffer VERTEX_LAYOUT;
	private final UIShader uiShader;
	private final OpenGLUIGPUDataStore uiDataStore;
	private final GLFWWindow window;

	public UIRendererSystem(GLFWWindow window) {
		this.window = window;
		uiShader = new UIShader(window);
		uiDataStore = new OpenGLUIGPUDataStore();

		VERTEX_LAYOUT = NkDrawVertexLayoutElement.create(4)
				.position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
				.position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8)
				.position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(16)
				.position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
				.flip();
	}

	@Override
	protected void processSystem() {
		RunOnce.run("UIRenderer init", uiShader::init);

		uiShader.startShader(null);

		scene();
		render();

		uiShader.stopShader();
	}

	private void render() {
		int height = window.getWindowHeight();

		GL.vaoContext(uiDataStore.getVAO(), () -> {
			// setup global state
			GL41.glEnable(GL41.GL_BLEND);
			GL41.glBlendEquation(GL41.GL_FUNC_ADD);
			GL41.glBlendFunc(GL41.GL_SRC_ALPHA, GL41.GL_ONE_MINUS_SRC_ALPHA);
			GL41.glDisable(GL41.GL_CULL_FACE);
			GL41.glDisable(GL41.GL_DEPTH_TEST);
			GL41.glEnable(GL41.GL_SCISSOR_TEST);
			GL41.glActiveTexture(GL41.GL_TEXTURE0);

			GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, uiDataStore.getVBO());
			GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, uiDataStore.getEBO());

			GL41.glBufferData(GL41.GL_ARRAY_BUFFER, MAX_VERTEX_BUFFER, GL41.GL_STREAM_DRAW);
			GL41.glBufferData(GL41.GL_ELEMENT_ARRAY_BUFFER, MAX_ELEMENT_BUFFER, GL41.GL_STREAM_DRAW);

			// load draw vertices & elements directly into vertex + element buffer
			ByteBuffer vertices = Objects.requireNonNull(GL41.glMapBuffer(GL41.GL_ARRAY_BUFFER, GL41.GL_WRITE_ONLY, MAX_VERTEX_BUFFER, null));
			ByteBuffer elements = Objects.requireNonNull(GL41.glMapBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, GL41.GL_WRITE_ONLY, MAX_ELEMENT_BUFFER, null));
			try (MemoryStack stack = stackPush()) {
				// fill convert configuration
				NkConvertConfig config = NkConvertConfig.callocStack(stack)
						.vertex_layout(VERTEX_LAYOUT)
						.vertex_size(20)
						.vertex_alignment(4)
						.null_texture(uiDataStore.getNullTexture())
						.circle_segment_count(22)
						.curve_segment_count(22)
						.arc_segment_count(22)
						.global_alpha(1.0f)
						.shape_AA(NK_ANTI_ALIASING_ON)
						.line_AA(NK_ANTI_ALIASING_ON);

				// setup buffers to load vertices and elements
				NkBuffer vbuf = NkBuffer.mallocStack(stack);
				NkBuffer ebuf = NkBuffer.mallocStack(stack);

				nk_buffer_init_fixed(vbuf, vertices/*, max_vertex_buffer*/);
				nk_buffer_init_fixed(ebuf, elements/*, max_element_buffer*/);
				nk_convert(uiDataStore.getContext(), uiDataStore.getCommands(), vbuf, ebuf, config);
			}
			GL41.glUnmapBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER);
			GL41.glUnmapBuffer(GL41.GL_ARRAY_BUFFER);

			// iterate over and execute each draw command
			float fb_scale_x = 1;//(float)display_width / (float)width;
			float fb_scale_y = 1;//(float)display_height / (float)height;

			long offset = NULL;
			for (NkDrawCommand cmd = nk__draw_begin(uiDataStore.getContext(), uiDataStore.getCommands()); cmd != null; cmd = nk__draw_next(cmd, uiDataStore.getCommands(), uiDataStore.getContext())) {
				if (cmd.elem_count() == 0) {
					continue;
				}
				GL41.glBindTexture(GL41.GL_TEXTURE_2D, cmd.texture().id());
				GL41.glScissor(
						(int) (cmd.clip_rect().x() * fb_scale_x),
						(int) ((height - (int) (cmd.clip_rect().y() + cmd.clip_rect().h())) * fb_scale_y),
						(int) (cmd.clip_rect().w() * fb_scale_x),
						(int) (cmd.clip_rect().h() * fb_scale_y)
				);
				GL41.glDrawElements(GL41.GL_TRIANGLES, cmd.elem_count(), GL41.GL_UNSIGNED_SHORT, offset);
				offset += cmd.elem_count() * 2;
			}
			nk_clear(uiDataStore.getContext());

			GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, 0);
			GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, 0);
			GL41.glDisable(GL41.GL_BLEND);
			GL41.glDisable(GL41.GL_SCISSOR_TEST);
		});
	}

	private void scene() {
		int x = 50;
		int y = 50;
		NkColorf background = NkColorf.create()
				.r(0.10f)
				.g(0.18f)
				.b(0.24f)
				.a(1.0f);
		IntBuffer compression = BufferUtils.createIntBuffer(1).put(0, 20);

		try (MemoryStack stack = stackPush()) {
			NkRect rect = NkRect.mallocStack(stack);

			if (nk_begin(
					uiDataStore.getContext(),
					"Demo",
					nk_rect(x, y, 230, 250, rect),
					NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_SCALABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE
			)) {
				nk_layout_row_static(uiDataStore.getContext(), 30, 80, 1);
				if (nk_button_label(uiDataStore.getContext(), "button")) {
					System.out.println("button pressed");
				}

				nk_layout_row_dynamic(uiDataStore.getContext(), 30, 2);
				nk_option_label(uiDataStore.getContext(), "easy", true);

				nk_layout_row_dynamic(uiDataStore.getContext(), 25, 1);
				nk_property_int(uiDataStore.getContext(), "Compression:", 0, compression, 100, 10, 1);

				nk_layout_row_dynamic(uiDataStore.getContext(), 20, 1);
				nk_label(uiDataStore.getContext(), "background:", NK_TEXT_LEFT);
				nk_layout_row_dynamic(uiDataStore.getContext(), 25, 1);
				if (nk_combo_begin_color(uiDataStore.getContext(), nk_rgb_cf(background, NkColor.mallocStack(stack)), NkVec2.mallocStack(stack).set(nk_widget_width(uiDataStore.getContext()), 400))) {
					nk_layout_row_dynamic(uiDataStore.getContext(), 120, 1);
					nk_color_picker(uiDataStore.getContext(), background, NK_RGBA);
					nk_layout_row_dynamic(uiDataStore.getContext(), 25, 1);
					background
							.r(nk_propertyf(uiDataStore.getContext(), "#R:", 0, background.r(), 1.0f, 0.01f, 0.005f))
							.g(nk_propertyf(uiDataStore.getContext(), "#G:", 0, background.g(), 1.0f, 0.01f, 0.005f))
							.b(nk_propertyf(uiDataStore.getContext(), "#B:", 0, background.b(), 1.0f, 0.01f, 0.005f))
							.a(nk_propertyf(uiDataStore.getContext(), "#A:", 0, background.a(), 1.0f, 0.01f, 0.005f));
					nk_combo_end(uiDataStore.getContext());
				}
			}
			nk_end(uiDataStore.getContext());
		}
	}
}
