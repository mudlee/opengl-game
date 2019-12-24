package spck.engine.ui;

import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.*;
import org.lwjgl.opengl.GL41;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.util.ResourceLoader;
import spck.engine.window.GLFWWindow;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class NuklearHandler {
    private static final NkAllocator ALLOCATOR;
    private static final NkDrawVertexLayoutElement.Buffer VERTEX_LAYOUT;
    private static final Logger LOGGER = LoggerFactory.getLogger(NuklearHandler.class);
    private static final int BUFFER_INITIAL_SIZE = 4 * 1024;
    private static final int MAX_VERTEX_BUFFER = 512 * 1024;
    private static final int MAX_ELEMENT_BUFFER = 128 * 1024;

    private final NkContext ctx = NkContext.create();
    private final NkUserFont default_font = NkUserFont.create();
    private final NkBuffer cmds = NkBuffer.create();
    private final NkDrawNullTexture null_texture = NkDrawNullTexture.create();
    private final GLFWWindow window;

    private int vbo, vao, ebo;
    private int prog;
    private int vert_shdr;
    private int frag_shdr;
    private int uniform_tex;
    private int uniform_proj;

    static {
        ALLOCATOR = NkAllocator.create()
                .alloc((handle, old, size) -> nmemAllocChecked(size))
                .mfree((handle, ptr) -> nmemFree(ptr));

        VERTEX_LAYOUT = NkDrawVertexLayoutElement.create(4)
                .position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
                .position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8)
                .position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(16)
                .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
                .flip();
    }

    public NuklearHandler(GLFWWindow window) {
        this.window = window;
        MessageBus.register(LifeCycle.GAME_START.eventID(), this::onStart);
        MessageBus.register(LifeCycle.FRAME_START.eventID(), this::onFrameStart);
    }

    private void onStart() {
        LOGGER.debug("Initializing {}...", NuklearHandler.class.getName());
        setupWindow(window.getId());
        initStb();
    }

    private static final int EASY = 0;
    private static final int HARD = 1;
    private int op = EASY;
    private IntBuffer compression = BufferUtils.createIntBuffer(1).put(0, 20);
    NkColorf background = NkColorf.create()
            .r(0.10f)
            .g(0.18f)
            .b(0.24f)
            .a(1.0f);

    private void onFrameStart() {
        nk_input_begin(ctx);
        NkMouse mouse = ctx.input().mouse();
        if (mouse.grab()) {
            glfwSetInputMode(window.getId(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        } else if (mouse.grabbed()) {
            float prevX = mouse.prev().x();
            float prevY = mouse.prev().y();
            glfwSetCursorPos(window.getId(), prevX, prevY);
            mouse.pos().x(prevX);
            mouse.pos().y(prevY);
        } else if (mouse.ungrab()) {
            glfwSetInputMode(window.getId(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }

        nk_input_end(ctx);

        int x = 50;
        int y = 50;

        try (MemoryStack stack = stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);

            if (nk_begin(
                    ctx,
                    "Demo",
                    nk_rect(x, y, 230, 250, rect),
                    NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_SCALABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE
            )) {
                nk_layout_row_static(ctx, 30, 80, 1);
                if (nk_button_label(ctx, "button")) {
                    System.out.println("button pressed");
                }

                nk_layout_row_dynamic(ctx, 30, 2);
                if (nk_option_label(ctx, "easy", op == EASY)) {
                    op = EASY;
                }
                if (nk_option_label(ctx, "hard", op == HARD)) {
                    op = HARD;
                }

                nk_layout_row_dynamic(ctx, 25, 1);
                nk_property_int(ctx, "Compression:", 0, compression, 100, 10, 1);

                nk_layout_row_dynamic(ctx, 20, 1);
                nk_label(ctx, "background:", NK_TEXT_LEFT);
                nk_layout_row_dynamic(ctx, 25, 1);
                if (nk_combo_begin_color(ctx, nk_rgb_cf(background, NkColor.mallocStack(stack)), NkVec2.mallocStack(stack).set(nk_widget_width(ctx), 400))) {
                    nk_layout_row_dynamic(ctx, 120, 1);
                    nk_color_picker(ctx, background, NK_RGBA);
                    nk_layout_row_dynamic(ctx, 25, 1);
                    background
                            .r(nk_propertyf(ctx, "#R:", 0, background.r(), 1.0f, 0.01f, 0.005f))
                            .g(nk_propertyf(ctx, "#G:", 0, background.g(), 1.0f, 0.01f, 0.005f))
                            .b(nk_propertyf(ctx, "#B:", 0, background.b(), 1.0f, 0.01f, 0.005f))
                            .a(nk_propertyf(ctx, "#A:", 0, background.a(), 1.0f, 0.01f, 0.005f));
                    nk_combo_end(ctx);
                }
            }
            nk_end(ctx);
        }

        render(NK_ANTI_ALIASING_ON, MAX_VERTEX_BUFFER, MAX_ELEMENT_BUFFER);
    }

    private void render(int AA, int max_vertex_buffer, int max_element_buffer) {
        int height = window.getWindowHeight();
        int width = window.getWindowWidth();
        int display_width = width;
        int display_height = height;

        try (MemoryStack stack = stackPush()) {
            // setup global state
            GL41.glEnable(GL41.GL_BLEND);
            GL41.glBlendEquation(GL41.GL_FUNC_ADD);
            GL41.glBlendFunc(GL41.GL_SRC_ALPHA, GL41.GL_ONE_MINUS_SRC_ALPHA);
            GL41.glDisable(GL41.GL_CULL_FACE);
            GL41.glDisable(GL41.GL_DEPTH_TEST);
            GL41.glEnable(GL41.GL_SCISSOR_TEST);
            GL41.glActiveTexture(GL41.GL_TEXTURE0);

            // setup program
            GL41.glUseProgram(prog);
            GL41.glUniform1i(uniform_tex, 0);
            GL41.glUniformMatrix4fv(uniform_proj, false, stack.floats(
                    2.0f / width, 0.0f, 0.0f, 0.0f,
                    0.0f, -2.0f / height, 0.0f, 0.0f,
                    0.0f, 0.0f, -1.0f, 0.0f,
                    -1.0f, 1.0f, 0.0f, 1.0f
            ));
            GL41.glViewport(0, 0, display_width, display_height);
        }

        {
            // convert from command queue into draw list and draw to screen

            // allocate vertex and element buffer
            GL41.glBindVertexArray(vao);
            GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vbo);
            GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, ebo);

            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, max_vertex_buffer, GL41.GL_STREAM_DRAW);
            GL41.glBufferData(GL41.GL_ELEMENT_ARRAY_BUFFER, max_element_buffer, GL41.GL_STREAM_DRAW);

            // load draw vertices & elements directly into vertex + element buffer
            ByteBuffer vertices = Objects.requireNonNull(GL41.glMapBuffer(GL41.GL_ARRAY_BUFFER, GL41.GL_WRITE_ONLY, max_vertex_buffer, null));
            ByteBuffer elements = Objects.requireNonNull(GL41.glMapBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, GL41.GL_WRITE_ONLY, max_element_buffer, null));
            try (MemoryStack stack = stackPush()) {
                // fill convert configuration
                NkConvertConfig config = NkConvertConfig.callocStack(stack)
                        .vertex_layout(VERTEX_LAYOUT)
                        .vertex_size(20)
                        .vertex_alignment(4)
                        .null_texture(null_texture)
                        .circle_segment_count(22)
                        .curve_segment_count(22)
                        .arc_segment_count(22)
                        .global_alpha(1.0f)
                        .shape_AA(AA)
                        .line_AA(AA);

                // setup buffers to load vertices and elements
                NkBuffer vbuf = NkBuffer.mallocStack(stack);
                NkBuffer ebuf = NkBuffer.mallocStack(stack);

                nk_buffer_init_fixed(vbuf, vertices/*, max_vertex_buffer*/);
                nk_buffer_init_fixed(ebuf, elements/*, max_element_buffer*/);
                nk_convert(ctx, cmds, vbuf, ebuf, config);
            }
            GL41.glUnmapBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER);
            GL41.glUnmapBuffer(GL41.GL_ARRAY_BUFFER);

            // iterate over and execute each draw command
            float fb_scale_x = 1;//(float)display_width / (float)width;
            float fb_scale_y = 1;//(float)display_height / (float)height;

            long offset = NULL;
            for (NkDrawCommand cmd = nk__draw_begin(ctx, cmds); cmd != null; cmd = nk__draw_next(cmd, cmds, ctx)) {
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
            nk_clear(ctx);
        }

        // default OpenGL state
        GL41.glUseProgram(0);
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, 0);
        GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL41.glBindVertexArray(0);
        GL41.glDisable(GL41.GL_BLEND);
        GL41.glDisable(GL41.GL_SCISSOR_TEST);
    }

    private void initStb() {
        LOGGER.debug("Creating font...");
        ByteBuffer ttf = ResourceLoader.loadToByteBuffer("/fonts/GeosansLight.ttf");
        int BITMAP_W = 1024;
        int BITMAP_H = 1024;

        int FONT_HEIGHT = 18;
        int fontTexID = GL41.glGenTextures();

        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(95);

        float scale;
        float descent;

        try (MemoryStack stack = stackPush()) {
            stbtt_InitFont(fontInfo, ttf);
            scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);

            IntBuffer d = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, null, d, null);
            descent = d.get(0) * scale;

            ByteBuffer bitmap = memAlloc(BITMAP_W * BITMAP_H);

            STBTTPackContext pc = STBTTPackContext.mallocStack(stack);
            stbtt_PackBegin(pc, bitmap, BITMAP_W, BITMAP_H, 0, 1, NULL);
            stbtt_PackSetOversampling(pc, 4, 4);
            stbtt_PackFontRange(pc, ttf, 0, FONT_HEIGHT, 32, cdata);
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

        default_font
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

                        stbtt_GetPackedQuad(cdata, BITMAP_W, BITMAP_H, codepoint - 32, x, y, q, false);
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

        nk_style_set_font(ctx, default_font);
    }

    private NkContext setupWindow(long win) {
        glfwSetScrollCallback(win, (window, xoffset, yoffset) -> {
            try (MemoryStack stack = stackPush()) {
                NkVec2 scroll = NkVec2.mallocStack(stack)
                        .x((float) xoffset)
                        .y((float) yoffset);
                nk_input_scroll(ctx, scroll);
            }
        });
        glfwSetCharCallback(win, (window, codepoint) -> nk_input_unicode(ctx, codepoint));
        /*glfwSetKeyCallback(win, (window, key, scancode, action, mods) -> {
            boolean press = action == GLFW_PRESS;
            switch (key) {
                case GLFW_KEY_ESCAPE:
                    glfwSetWindowShouldClose(window, true);
                    break;
                case GLFW_KEY_DELETE:
                    nk_input_key(ctx, NK_KEY_DEL, press);
                    break;
                case GLFW_KEY_ENTER:
                    nk_input_key(ctx, NK_KEY_ENTER, press);
                    break;
                case GLFW_KEY_TAB:
                    nk_input_key(ctx, NK_KEY_TAB, press);
                    break;
                case GLFW_KEY_BACKSPACE:
                    nk_input_key(ctx, NK_KEY_BACKSPACE, press);
                    break;
                case GLFW_KEY_UP:
                    nk_input_key(ctx, NK_KEY_UP, press);
                    break;
                case GLFW_KEY_DOWN:
                    nk_input_key(ctx, NK_KEY_DOWN, press);
                    break;
                case GLFW_KEY_HOME:
                    nk_input_key(ctx, NK_KEY_TEXT_START, press);
                    nk_input_key(ctx, NK_KEY_SCROLL_START, press);
                    break;
                case GLFW_KEY_END:
                    nk_input_key(ctx, NK_KEY_TEXT_END, press);
                    nk_input_key(ctx, NK_KEY_SCROLL_END, press);
                    break;
                case GLFW_KEY_PAGE_DOWN:
                    nk_input_key(ctx, NK_KEY_SCROLL_DOWN, press);
                    break;
                case GLFW_KEY_PAGE_UP:
                    nk_input_key(ctx, NK_KEY_SCROLL_UP, press);
                    break;
                case GLFW_KEY_LEFT_SHIFT:
                case GLFW_KEY_RIGHT_SHIFT:
                    nk_input_key(ctx, NK_KEY_SHIFT, press);
                    break;
                case GLFW_KEY_LEFT_CONTROL:
                case GLFW_KEY_RIGHT_CONTROL:
                    if (press) {
                        nk_input_key(ctx, NK_KEY_COPY, glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_PASTE, glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_CUT, glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_TEXT_UNDO, glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_TEXT_REDO, glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_TEXT_WORD_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_TEXT_WORD_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_TEXT_LINE_START, glfwGetKey(window, GLFW_KEY_B) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_TEXT_LINE_END, glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS);
                    } else {
                        nk_input_key(ctx, NK_KEY_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
                        nk_input_key(ctx, NK_KEY_COPY, false);
                        nk_input_key(ctx, NK_KEY_PASTE, false);
                        nk_input_key(ctx, NK_KEY_CUT, false);
                        nk_input_key(ctx, NK_KEY_SHIFT, false);
                    }
                    break;
            }
        });
        glfwSetCursorPosCallback(win, (window, xpos, ypos) -> nk_input_motion(ctx, (int)xpos, (int)ypos));
        glfwSetMouseButtonCallback(win, (window, button, action, mods) -> {
            try (MemoryStack stack = stackPush()) {
                DoubleBuffer cx = stack.mallocDouble(1);
                DoubleBuffer cy = stack.mallocDouble(1);

                glfwGetCursorPos(window, cx, cy);

                int x = (int)cx.get(0);
                int y = (int)cy.get(0);

                int nkButton;
                switch (button) {
                    case GLFW_MOUSE_BUTTON_RIGHT:
                        nkButton = NK_BUTTON_RIGHT;
                        break;
                    case GLFW_MOUSE_BUTTON_MIDDLE:
                        nkButton = NK_BUTTON_MIDDLE;
                        break;
                    default:
                        nkButton = NK_BUTTON_LEFT;
                }
                nk_input_button(ctx, nkButton, x, y, action == GLFW_PRESS);
            }
        });*/

        nk_init(ctx, ALLOCATOR, null);
        ctx.clip()
                .copy((handle, text, len) -> {
                    if (len == 0) {
                        return;
                    }

                    try (MemoryStack stack = stackPush()) {
                        ByteBuffer str = stack.malloc(len + 1);
                        memCopy(text, memAddress(str), len);
                        str.put(len, (byte) 0);

                        glfwSetClipboardString(win, str);
                    }
                })
                .paste((handle, edit) -> {
                    long text = nglfwGetClipboardString(win);
                    if (text != NULL) {
                        nnk_textedit_paste(edit, text, nnk_strlen(text));
                    }
                });

        setupContext();
        return ctx;
    }

    private void setupContext() {
        String NK_SHADER_VERSION = Platform.get() == Platform.MACOSX ? "#version 150\n" : "#version 300 es\n";
        String vertex_shader =
                NK_SHADER_VERSION +
                        "uniform mat4 ProjMtx;\n" +
                        "in vec2 Position;\n" +
                        "in vec2 TexCoord;\n" +
                        "in vec4 Color;\n" +
                        "out vec2 Frag_UV;\n" +
                        "out vec4 Frag_Color;\n" +
                        "void main() {\n" +
                        "   Frag_UV = TexCoord;\n" +
                        "   Frag_Color = Color;\n" +
                        "   gl_Position = ProjMtx * vec4(Position.xy, 0, 1);\n" +
                        "}\n";
        String fragment_shader =
                NK_SHADER_VERSION +
                        "precision mediump float;\n" +
                        "uniform sampler2D Texture;\n" +
                        "in vec2 Frag_UV;\n" +
                        "in vec4 Frag_Color;\n" +
                        "out vec4 Out_Color;\n" +
                        "void main(){\n" +
                        "   Out_Color = Frag_Color * texture(Texture, Frag_UV.st);\n" +
                        "}\n";

        nk_buffer_init(cmds, ALLOCATOR, BUFFER_INITIAL_SIZE);

        // SHADING
        prog = GL41.glCreateProgram();
        vert_shdr = GL41.glCreateShader(GL41.GL_VERTEX_SHADER);
        frag_shdr = GL41.glCreateShader(GL41.GL_FRAGMENT_SHADER);
        GL41.glShaderSource(vert_shdr, vertex_shader);
        GL41.glShaderSource(frag_shdr, fragment_shader);
        GL41.glCompileShader(vert_shdr);
        GL41.glCompileShader(frag_shdr);
        if (GL41.glGetShaderi(vert_shdr, GL41.GL_COMPILE_STATUS) != GL41.GL_TRUE) {
            throw new IllegalStateException();
        }
        if (GL41.glGetShaderi(frag_shdr, GL41.GL_COMPILE_STATUS) != GL41.GL_TRUE) {
            throw new IllegalStateException();
        }
        GL41.glAttachShader(prog, vert_shdr);
        GL41.glAttachShader(prog, frag_shdr);
        GL41.glLinkProgram(prog);
        if (GL41.glGetProgrami(prog, GL41.GL_LINK_STATUS) != GL41.GL_TRUE) {
            throw new IllegalStateException();
        }

        uniform_tex = GL41.glGetUniformLocation(prog, "Texture");
        uniform_proj = GL41.glGetUniformLocation(prog, "ProjMtx");
        // SHADING

        int attrib_pos = GL41.glGetAttribLocation(prog, "Position");
        int attrib_uv = GL41.glGetAttribLocation(prog, "TexCoord");
        int attrib_col = GL41.glGetAttribLocation(prog, "Color");

        {
            // buffer setup
            vbo = GL41.glGenBuffers();
            ebo = GL41.glGenBuffers();
            vao = GL41.glGenVertexArrays();

            GL41.glBindVertexArray(vao);
            GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vbo);
            GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, ebo);

            GL41.glEnableVertexAttribArray(attrib_pos);
            GL41.glEnableVertexAttribArray(attrib_uv);
            GL41.glEnableVertexAttribArray(attrib_col);

            GL41.glVertexAttribPointer(attrib_pos, 2, GL41.GL_FLOAT, false, 20, 0);
            GL41.glVertexAttribPointer(attrib_uv, 2, GL41.GL_FLOAT, false, 20, 8);
            GL41.glVertexAttribPointer(attrib_col, 4, GL41.GL_UNSIGNED_BYTE, true, 20, 16);
        }

        {
            // null texture setup
            int nullTexID = GL41.glGenTextures();

            null_texture.texture().id(nullTexID);
            null_texture.uv().set(0.5f, 0.5f);

            GL41.glBindTexture(GL41.GL_TEXTURE_2D, nullTexID);
            try (MemoryStack stack = stackPush()) {
                GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA8, 1, 1, 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_INT_8_8_8_8_REV, stack.ints(0xFFFFFFFF));
            }
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);
        }

        GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, 0);
        GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL41.glBindVertexArray(0);
    }
}
