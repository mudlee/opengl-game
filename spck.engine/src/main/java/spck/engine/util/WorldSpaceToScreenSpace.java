package spck.engine.util;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import spck.engine.Engine;

public class WorldSpaceToScreenSpace {
    private static final Vector4f WORLD_POS_TEMP = new Vector4f();
    private static final Vector4f CLIP_SPACE_TEMP = new Vector4f();
    private static final Vector3f NDC_SPACE_TEMP = new Vector3f();
    private static final Vector2f WINDOW_SPACE_TEMP = new Vector2f();
    private static final Vector2f WINDOW_SIZE_TEMP = new Vector2f();

    public static Vector2f convert(Vector3f worldPosition, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        WORLD_POS_TEMP.set(worldPosition, 1f);
        WINDOW_SIZE_TEMP.set(Engine.window.getPreferences().getWidth(), Engine.window.getPreferences().getHeight());

        CLIP_SPACE_TEMP.set(WORLD_POS_TEMP).mul(viewMatrix).mul(projectionMatrix);
        NDC_SPACE_TEMP.set(CLIP_SPACE_TEMP.x, -CLIP_SPACE_TEMP.y, CLIP_SPACE_TEMP.z).div(CLIP_SPACE_TEMP.w);
        WINDOW_SPACE_TEMP.set(NDC_SPACE_TEMP.x + 1f, NDC_SPACE_TEMP.y + 1f);
        WINDOW_SPACE_TEMP.set(WINDOW_SPACE_TEMP.x / 2f, WINDOW_SPACE_TEMP.y / 2f);
        WINDOW_SPACE_TEMP.mul(WINDOW_SIZE_TEMP);
        return WINDOW_SPACE_TEMP;
    }
}
