package spck.engine.render.textures;

public class Texture2D implements Texture {
    private final TextureRegistryID textureRegistryID;
    private final int id;
    private final int width;
    private final int height;
    private final int samplerIndex;
    private final int glTextureSlot;
    private final String shaderSamplerName;

    public Texture2D(TextureRegistryID textureRegistryID, int id, int width, int height, int samplerIndex, int glTextureSlot, String shaderSamplerName) {
        this.textureRegistryID = textureRegistryID;
        this.id = id;
        this.width = width;
        this.height = height;
        this.samplerIndex = samplerIndex;
        this.glTextureSlot = glTextureSlot;
        this.shaderSamplerName = shaderSamplerName;
    }

    @Override
    public Texture copy() {
        return new Texture2D(textureRegistryID, id, width, height, samplerIndex, glTextureSlot, shaderSamplerName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Texture2D)) return false;

        Texture2D texture2D = (Texture2D) o;

        return id == texture2D.id;
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
    public TextureRegistryID getTextureRegistryID() {
        return textureRegistryID;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}