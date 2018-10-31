package spck.engine.model;

import spck.engine.render.textures.TextureRegistryID;

public class ModelTextureRegistryID implements TextureRegistryID {
    private final String texturePath;

    ModelTextureRegistryID(String texturePath) {
        this.texturePath = texturePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelTextureRegistryID that = (ModelTextureRegistryID) o;

        return texturePath != null ? texturePath.equals(that.texturePath) : that.texturePath == null;
    }

    @Override
    public int hashCode() {
        return texturePath != null ? texturePath.hashCode() : 0;
    }
}

