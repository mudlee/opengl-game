package spck.engine.framework.assets;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.render.textures.TextureData;
import spck.engine.util.ResourceLoader;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.stbi_failure_reason;

public class TextureLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(TextureLoader.class);

    public static TextureData loadFromResources(String filePath) {
        LOGGER.debug("Loading Texture {}...", filePath);
        return loadFromByteBuffer(ResourceLoader.loadToByteBuffer(filePath));
    }

    public static TextureData loadFromByteBuffer(ByteBuffer byteBuffer) {
        LOGGER.debug("Loading Texture from ByteBuffer...");
        ByteBuffer image;
        int width;
        int height;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer components = stack.mallocInt(1);

            image = STBImage.stbi_load_from_memory(byteBuffer, w, h, components, 4);
            if (image == null) {
                LOGGER.error("Failed to load texture from ByteBuffer, reason: {}", stbi_failure_reason());
                throw new RuntimeException("Failed to load texture from ByteBuffer");
            }

            width = w.get();
            height = h.get();
        }

        LOGGER.debug("Texture loaded, {}x{}", width, height);

        return new TextureData(width, height, image);
    }
}
