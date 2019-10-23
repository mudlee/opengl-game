package spck.engine.model;

import spck.engine.render.textures.TextureRegistryID;

import java.util.Objects;

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

        return Objects.equals(texturePath, that.texturePath);
    }

    @Override
    public int hashCode() {
        return texturePath != null ? texturePath.hashCode() : 0;
    }
}

