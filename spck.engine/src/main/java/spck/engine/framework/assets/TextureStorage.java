package spck.engine.framework.assets;

import org.lwjgl.opengl.GL41;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.framework.GL;
import spck.engine.render.textures.Texture;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

public class TextureStorage {
    private final static List<Texture> TEXTURES = new ArrayList<>();
    private final static Map<String, Texture> CACHE = new HashMap<>();

    static {
        MessageBus.register(LifeCycle.CLEANUP.eventID(), TextureStorage::cleanUp);
    }

    private final static int[] GL_SLOTS = new int[]{
            GL41.GL_TEXTURE0,
            GL41.GL_TEXTURE1,
            GL41.GL_TEXTURE2,
            GL41.GL_TEXTURE3,
            GL41.GL_TEXTURE4,
            GL41.GL_TEXTURE5,
            GL41.GL_TEXTURE6,
            GL41.GL_TEXTURE7,
            GL41.GL_TEXTURE8,
            GL41.GL_TEXTURE9,
            GL41.GL_TEXTURE10,
            GL41.GL_TEXTURE11,
            GL41.GL_TEXTURE12,
            GL41.GL_TEXTURE13,
            GL41.GL_TEXTURE14,
            GL41.GL_TEXTURE15,
            GL41.GL_TEXTURE16,
            GL41.GL_TEXTURE17,
            GL41.GL_TEXTURE18,
            GL41.GL_TEXTURE19,
            GL41.GL_TEXTURE20,
            GL41.GL_TEXTURE21,
            GL41.GL_TEXTURE22,
            GL41.GL_TEXTURE23,
            GL41.GL_TEXTURE24,
            GL41.GL_TEXTURE25,
            GL41.GL_TEXTURE26,
            GL41.GL_TEXTURE27,
            GL41.GL_TEXTURE28,
            GL41.GL_TEXTURE29,
            GL41.GL_TEXTURE30,
            GL41.GL_TEXTURE31,
            // GL has a max of 31
    };

    public static Texture2D loadFromTextureData(TextureData textureData, String shaderSampler, String textureRegistryId) {
        if (CACHE.containsKey(textureRegistryId)) {
            return (Texture2D) CACHE.get(textureRegistryId);
        }

        int id = GL.genTextureContext(GL41.GL_TEXTURE_2D, textureId -> {
            GL41.glPixelStorei(GL41.GL_UNPACK_ALIGNMENT, 1);
            GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA, textureData.getWidth(), textureData.getHeight(), 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_BYTE, textureData.getImage());
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);
        });

        int nextIndex = getNextIndex();

        Texture2D texture = new Texture2D(textureRegistryId, id, textureData.getWidth(), textureData.getHeight(), nextIndex, GL_SLOTS[nextIndex], shaderSampler);
        TEXTURES.add(texture);
        CACHE.put(textureRegistryId, texture);

        return texture;
    }

    public static Texture2D loadFromResource(String file, String shaderSampler, String textureRegistryId) {
        TextureData textureData = TextureLoader.loadFromResources(file);

        int id = GL.genTextureContext(GL41.GL_TEXTURE_2D, textureId -> {
            GL41.glPixelStorei(GL41.GL_UNPACK_ALIGNMENT, 1);
            GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA, textureData.getWidth(), textureData.getHeight(), 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_BYTE, textureData.getImage());
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);
        });

        int nextIndex = getNextIndex();
        Texture2D texture = new Texture2D(textureRegistryId, id, textureData.getWidth(), textureData.getHeight(), nextIndex, GL_SLOTS[nextIndex], shaderSampler);
        TEXTURES.add(texture);
        return texture;
    }

    public static Texture2D loadFromResource(String file, String textureRegistryID) {
        return loadFromResource(file, null, textureRegistryID);
    }

    public static Texture2D store(String registryID, String sourcePath, int textureID, int width, int height, String shaderSampler) {
        int nextIndex = getNextIndex();

        Texture2D texture = new Texture2D(registryID, textureID, width, height, nextIndex, GL_SLOTS[nextIndex], shaderSampler);
        TEXTURES.add(texture);
        return texture;
    }

    public static void cleanUp() {
        TEXTURES.forEach(texture -> GL41.glDeleteTextures(texture.getId()));
    }

    private static int getNextIndex() {
        return TEXTURES.stream().mapToInt(Texture::getSamplerIndex).max().orElse(-1) + 1;
    }

    private static void premultiplyAlpha(ByteBuffer image, int w, int h, int stride) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * stride + x * 4;

                float alpha = (image.get(i + 3) & 0xFF) / 255.0f;
                image.put(i + 0, (byte) round(((image.get(i + 0) & 0xFF) * alpha)));
                image.put(i + 1, (byte) round(((image.get(i + 1) & 0xFF) * alpha)));
                image.put(i + 2, (byte) round(((image.get(i + 2) & 0xFF) * alpha)));
            }
        }
    }
}
