package spck.engine.render.textures;

public interface Texture {
    String getTextureRegistryId();

    int getId();

    int getSamplerIndex();

    String getShaderSamplerName();

    Texture copy();
}
