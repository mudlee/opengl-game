package spck.engine.render;

import org.joml.Vector3f;
import spck.engine.render.shader.Shader;
import spck.engine.render.textures.Texture2D;
import spck.engine.render.textures.TextureUVModifier;

import java.util.Optional;

public interface Material {
    void setSpecularColor(Vector3f specularColor);

    void setDiffuseColor(Vector3f diffuseColor);

    void setAmbientColor(Vector3f ambientColor);

    void setDiffuseTexture(Texture2D diffuseTexture);

    void setDiffuseTextureUVModifier(TextureUVModifier modifier);

    Vector3f getSpecularColor();

    Vector3f getDiffuseColor();

    Vector3f getAmbientColor();

    boolean hasDiffuseTexture();

    Texture2D getDiffuseTexture();

    Optional<TextureUVModifier> getDiffuseTextureUVModifier();

    Shader getShader();

    float getShininess();

    boolean isChanged();

    void ackChanges();

    void setNew(Material material);
}