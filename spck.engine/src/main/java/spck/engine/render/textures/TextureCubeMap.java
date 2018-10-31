package spck.engine.render.textures;

public class TextureCubeMap implements Texture {
    private final TextureRegistryID textureRegistryID;
    private final int id;
    private final int samplerIndex;
    private final int glTextureSlot;
    private final String shaderSamplerName;

    public TextureCubeMap(TextureRegistryID textureRegistryID, int id, int samplerIndex, int glTextureSlot, String shaderSamplerName) {
        this.textureRegistryID = textureRegistryID;
        this.id = id;
        this.samplerIndex = samplerIndex;
        this.glTextureSlot = glTextureSlot;
        this.shaderSamplerName = shaderSamplerName;
    }

    @Override
    public Texture copy() {
        return new TextureCubeMap(textureRegistryID, id, samplerIndex, glTextureSlot, shaderSamplerName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextureCubeMap texture = (TextureCubeMap) o;

        return id == texture.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String getShaderSamplerName() {
        return shaderSamplerName;
    }

    @Override
    public int getSamplerIndex() {
        return samplerIndex;
    }

    public int getGlTextureSlot() {
        return glTextureSlot;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public TextureRegistryID getTextureRegistryID() {
        return textureRegistryID;
    }
}

