package spck.engine.render.textures;

public interface Texture {
    TextureRegistryID getTextureRegistryID();

    int getId();

    int getSamplerIndex();

    String getShaderSamplerName();

    Texture copy();
}
