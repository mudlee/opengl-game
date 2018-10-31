package spck.engine.render.textures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TextureRegistry {
    private final static Logger LOGGER = LoggerFactory.getLogger(TextureRegistry.class);
    private static Map<TextureRegistryID, Texture> registry = new HashMap<>();

    public static Texture register(Texture entry) {
        LOGGER.debug("Registering {} ID:{} SAMPLER:{}-{}", entry.getClass().getSimpleName(), entry.getTextureRegistryID(), entry.getSamplerIndex(), entry.getShaderSamplerName());
        registry.putIfAbsent(entry.getTextureRegistryID(), entry);
        return entry;
    }

    public static Optional<Texture> get(TextureRegistryID textureID, boolean newInstance) {
        Texture texture = registry.getOrDefault(textureID, null);

        if (texture == null) {
            return Optional.empty();
        }

        if (newInstance) {
            return Optional.of(texture.copy());
        }

        return Optional.of(texture);
    }

    public static Collection<Texture> getAll() {
        return registry.values();
    }
}
