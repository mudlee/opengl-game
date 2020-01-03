package spck.engine.render.textures;

public class DynamicTextureRegistryID {
    private final String texturePath;

    public DynamicTextureRegistryID(String textureID) {
        this.texturePath = textureID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DynamicTextureRegistryID that = (DynamicTextureRegistryID) o;

        return texturePath != null ? texturePath.equals(that.texturePath) : that.texturePath == null;
    }

    @Override
    public int hashCode() {
        return texturePath != null ? texturePath.hashCode() : 0;
    }
}
