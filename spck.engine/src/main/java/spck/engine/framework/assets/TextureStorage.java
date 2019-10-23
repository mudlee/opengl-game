package spck.engine.framework.assets;


import org.lwjgl.opengl.GL41;
import spck.engine.framework.GL;
import spck.engine.render.textures.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureStorage {
    private final static List<Texture> TEXTURES = new ArrayList<>();
    private final static Map<TextureRegistryID, Texture> CACHE = new HashMap<>();

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

    public static TextureCubeMap loadCubeMapFromResource(String[] files, String shaderSampler, TextureRegistryID textureRegistryID) {
        if (files.length != 6) {
            throw new RuntimeException("CubeMaps have to have 6 sides, not " + files.length);
        }

        int id = GL.genTextureContext(GL41.GL_TEXTURE_CUBE_MAP, textureId -> {
            for (int i = 0; i < files.length; i++) {
                TextureData textureData = TextureLoader.loadFromResources(files[i]);
                GL41.glTexImage2D(GL41.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL41.GL_RGBA, textureData.getWidth(), textureData.getHeight(), 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_BYTE, textureData.getImage());
            }

            GL41.glTexParameteri(GL41.GL_TEXTURE_CUBE_MAP, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_LINEAR);
            GL41.glTexParameteri(GL41.GL_TEXTURE_CUBE_MAP, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_LINEAR);
        });

        int nextIndex = TEXTURES.stream().mapToInt(Texture::getSamplerIndex).max().orElse(-1) + 1;

        TextureCubeMap texture = new TextureCubeMap(textureRegistryID, id, nextIndex, GL_SLOTS[nextIndex], shaderSampler);
        TEXTURES.add(texture);
        return texture;
    }

    public static Texture2D loadFromTextureData(TextureData textureData, String shaderSampler, TextureRegistryID textureRegistryID) {
        if (CACHE.containsKey(textureRegistryID)) {
            return (Texture2D) CACHE.get(textureRegistryID);
        }

        int id = GL.genTextureContext(GL41.GL_TEXTURE_2D, textureId -> {
            GL41.glPixelStorei(GL41.GL_UNPACK_ALIGNMENT, 1);
            GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA, textureData.getWidth(), textureData.getHeight(), 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_BYTE, textureData.getImage());
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);
        });

        int nextIndex = getNextIndex();

        Texture2D texture = new Texture2D(textureRegistryID, id, textureData.getWidth(), textureData.getHeight(), nextIndex, GL_SLOTS[nextIndex], shaderSampler);
        TEXTURES.add(texture);
        CACHE.put(textureRegistryID, texture);

        return texture;
    }

    public static Texture2D loadFromResource(String file, String shaderSampler, TextureRegistryID textureRegistryID) {
        TextureData textureData = TextureLoader.loadFromResources(file);

        int id = GL.genTextureContext(GL41.GL_TEXTURE_2D, textureId -> {
            GL41.glPixelStorei(GL41.GL_UNPACK_ALIGNMENT, 1);
            GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA, textureData.getWidth(), textureData.getHeight(), 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_BYTE, textureData.getImage());
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);
        });

        int nextIndex = getNextIndex();
        Texture2D texture = new Texture2D(textureRegistryID, id, textureData.getWidth(), textureData.getHeight(), nextIndex, GL_SLOTS[nextIndex], shaderSampler);
        TEXTURES.add(texture);
        return texture;
    }

    public static Texture2D loadFromResource(String file, TextureRegistryID textureRegistryID) {
        return loadFromResource(file, null, textureRegistryID);
    }

    public static Texture2D store(TextureRegistryID registryID, String sourcePath, int textureID, int width, int height, String shaderSampler) {
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
}
