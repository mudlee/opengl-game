package spck.engine.render;

import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AABB {
    private final static Logger LOGGER = LoggerFactory.getLogger(AABB.class);
    private final float[] vertices;
    private final int[] indices;
    private final Vector3f min;
    private final Vector3f max;

    public AABB(Vector3f min, Vector3f max) {
        min.x -= 0.2f;
        min.y -= 0.2f;
        min.z -= 0.2f;
        max.x += 0.2f;
        max.y += 0.2f;
        max.z += 0.2f;

        this.min = min;
        this.max = max;
        vertices = new float[]{
                min.x, min.y, max.z, // front bottom left & left bottom right- 0
                max.x, min.y, max.z, // front bottom right & right bottom left & bottom top right - 1
                min.x, max.y, max.z, // front top left & left top right & top bottom left- 2
                max.x, max.y, max.z, // front top right & right top left & top bottom right - 3

                max.x, max.y, min.z, // right top right & back top left & top top right - 4
                max.x, min.y, min.z, // right bottom right & back bottom left & bottom bottom right - 5
                min.x, max.y, min.z, // back top right & left top left & top top left - 6
                min.x, min.y, min.z, // back bottom right & left bottom left & bottom bottom left- 7
        };

        LOGGER.debug("{}", vertices);

        indices = new int[]{
                0, 1, 2, 1, 3, 2, // front
                3, 1, 5, 3, 5, 4, // right
                4, 5, 7, 7, 6, 4, // back
                6, 7, 0, 0, 2, 6, // left
                0, 7, 5, 5, 1, 0, // bottom
                2, 3, 4, 4, 6, 2, // top
        };
    }

    @Override
    public String toString() {
        return "AABB{" +
                ", min=" + min +
                ", max=" + max +
                '}';
    }

    public float[] getVertices() {
        // TODO: recalculate if rotate
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }
}
