package spck.engine.framework;

import org.lwjgl.opengl.GL41;
import spck.engine.render.textures.Texture2D;

import java.util.function.Consumer;

public class GL {
    public static int genTextureContext(int target, Consumer<Integer> consumer) {
        int textureId = GL41.glGenTextures();

        GL41.glBindTexture(target, textureId);
        consumer.accept(textureId);
        GL41.glBindTexture(target, 0);
        return textureId;
    }

    public static void activateTexture2D(Texture2D... textures) {
        for (Texture2D texture : textures) {
            GL41.glActiveTexture(texture.getGlTextureSlot());
            GL41.glBindTexture(GL41.GL_TEXTURE_2D, texture.getId());
        }
    }

    public static void deActivateTexture2D(Texture2D... textures) {
        for (Texture2D texture : textures) {
            GL41.glActiveTexture(texture.getGlTextureSlot());
            GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);
        }
    }

    public static void genFramebufferContext(Consumer<Integer> consumer) {
        int fboId = GL41.glGenFramebuffers();
        GL.framebufferContext(fboId, () -> consumer.accept(fboId));
    }

    public static void genRenderbufferContext(Consumer<Integer> consumer) {
        int renderBufferId = GL41.glGenRenderbuffers();

        GL41.glBindRenderbuffer(GL41.GL_RENDERBUFFER, renderBufferId);
        consumer.accept(renderBufferId);
        GL41.glBindRenderbuffer(GL41.GL_RENDERBUFFER, 0);
    }

    public static void framebufferContext(int framebufferId, Runnable runnable) {
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, framebufferId);
        runnable.run();
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);
    }

    public static void genVaoContext(Consumer<Integer> consumer, Runnable vaoUnbindedCallback) {
        int vaoId = GL41.glGenVertexArrays();
        GL.vaoContext(vaoId, () -> consumer.accept(vaoId), vaoUnbindedCallback);
    }

    public static void vaoContext(int vaoId, Runnable runnable, Runnable vaoUnbindedCallback) {
        GL.vaoContext(vaoId, runnable);
        vaoUnbindedCallback.run();
    }

    public static void vaoContext(int vaoId, Runnable runnable) {
        GL41.glBindVertexArray(vaoId);
        runnable.run();
        GL41.glBindVertexArray(0);
    }

    public static void bufferContext(int bufferId, Runnable runnable) {
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, bufferId);
        runnable.run();
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, 0);
    }

    public static int genBufferContext(Consumer<Integer> consumer) {
        int vboId = GL41.glGenBuffers();
        GL.bufferContext(vboId, () -> consumer.accept(vboId));
        return vboId;
    }
}
