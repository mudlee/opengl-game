package spck.engine.render;

import org.joml.Vector3f;
import spck.engine.Engine;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureUVModifier;

import java.util.Optional;

public class DefaultMaterial implements Material {
    private final Shader shader;
    private final Renderer renderer;
    private float shininess = 1.0f;
    private Vector3f specularColor = new Vector3f(0.5f, 0.5f, 0.5f);
    private Vector3f diffuseColor = new Vector3f(0.5f, 0.5f, 0.5f);
    private Vector3f ambientColor = new Vector3f(0.5f, 0.5f, 0.5f);
    private Texture2D diffuseTexture = null;
    private TextureUVModifier diffuseTextureUVModifier = null;
    private boolean changed;

    public DefaultMaterial() {
        shader = Engine.shader;
        renderer = Engine.renderer;
    }

    /*
     * We consider DefaultMaterial equal if they have the same color and texture settings
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultMaterial)) return false;

        DefaultMaterial that = (DefaultMaterial) o;

        if (Float.compare(that.shininess, shininess) != 0) return false;
        if (!specularColor.equals(that.specularColor)) return false;
        if (!diffuseColor.equals(that.diffuseColor)) return false;
        if (!ambientColor.equals(that.ambientColor)) return false;
        return diffuseTexture != null ? diffuseTexture.equals(that.diffuseTexture) : that.diffuseTexture == null;
    }

    @Override
    public int hashCode() {
        int result = (shininess != +0.0f ? Float.floatToIntBits(shininess) : 0);
        result = 31 * result + specularColor.hashCode();
        result = 31 * result + diffuseColor.hashCode();
        result = 31 * result + ambientColor.hashCode();
        result = 31 * result + (diffuseTexture != null ? diffuseTexture.hashCode() : 0);
        return result;
    }

    @Override
    public void setSpecularColor(Vector3f specularColor) {
        this.specularColor = specularColor;
        changed = true;
    }

    @Override
    public void setDiffuseColor(Vector3f diffuseColor) {
        this.diffuseColor = diffuseColor;
        changed = true;
    }

    @Override
    public void setAmbientColor(Vector3f ambientColor) {
        this.ambientColor = ambientColor;
        changed = true;
    }

    @Override
    public void setDiffuseTexture(Texture2D diffuseTexture) {
        this.diffuseTexture = diffuseTexture;
        changed = true;
    }

    @Override
    public void setDiffuseTextureUVModifier(TextureUVModifier modifier) {
        this.diffuseTextureUVModifier = modifier;
    }

    @Override
    public Texture2D getDiffuseTexture() {
        return diffuseTexture;
    }

    @Override
    public Optional<TextureUVModifier> getDiffuseTextureUVModifier() {
        return Optional.ofNullable(diffuseTextureUVModifier);
    }

    @Override
    public Vector3f getSpecularColor() {
        return specularColor;
    }

    @Override
    public Vector3f getDiffuseColor() {
        return diffuseColor;
    }

    @Override
    public Vector3f getAmbientColor() {
        return ambientColor;
    }

    @Override
    public boolean hasDiffuseTexture() {
        return diffuseTexture != null;
    }

    @Override
    public Shader getShader() {
        return shader;
    }

    @Override
    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public float getShininess() {
        return shininess;
    }

    @Override
    public void processChanges(Runnable callback) {
        if (changed) {
            changed = false;
            callback.run();
        }
    }

    @Override
    public void setNew(Material material) {
        shininess = material.getShininess();
        diffuseColor = material.getDiffuseColor();
        specularColor = material.getSpecularColor();
        diffuseTexture = material.getDiffuseTexture();
        ambientColor = material.getAmbientColor();
        changed = true;
    }

    @Override
    public String toString() {
        return "DefaultMaterial{" +
                "hashcode='" + hashCode() + '\'' +
                '}';
    }
}

