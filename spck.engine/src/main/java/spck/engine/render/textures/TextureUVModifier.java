package spck.engine.render.textures;

import org.joml.Vector2f;

/**
 * It holds information about a texture atlas item.
 */
public class TextureUVModifier {
    private float scale;
    private Vector2f offset;

    /**
     * @param scale  How many should be item scaled down comparing to the atlas. Atlas expected to be a quad, like a 4X4
     * @param offset The x and y offset on the atlas
     */
    public TextureUVModifier(float scale, Vector2f offset) {
        this.scale = scale;
        this.offset = offset;
    }

    /**
     * @return How many should be item scaled down comparing to the atlas
     */
    public float getScale() {
        return scale;
    }

    /**
     * @return x and y offset on the atlas
     */
    public Vector2f getOffset() {
        return offset;
    }
}
