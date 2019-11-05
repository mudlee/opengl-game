package spck.engine.render;

import org.joml.Vector3f;

public class AABB {
    private final float[] vertices;
    private final float[] indices;
    private final Vector3f min;
    private final Vector3f max;

    public AABB(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;
        /*vertices = new float[]{
            min.x, max.y, max.z, // V0
            min.x, min.y, max.z, // V1
            max.x, min.y, max.z, // V2
            max.x, max.y, max.z, // V3
            min.x, max.y, min.z, // V4
            max.x, max.y, min.z, // V5
            min.x, min.y, min.z, // V6
            max.x, min.y, min.z, // V7
        };

        indices = new float[]{
            0,1,3,3,1,2,
            4,0,3,5,4,3,
            3,2,7,5,3,7,
            6,1,0,6,0,4,
            2,1,6,2,6,7,
            7,6,4,7,4,5,
        };*/
        vertices = new float[]{
                max.x, min.y, min.z,
                max.x, min.y, max.z,
                min.x, min.y, min.z,
                max.x, max.y, min.z,
                min.x, max.y, min.z,
                max.x, max.y, max.z,
                max.x, min.y, min.z,
                max.x, max.y, min.z,
                max.x, min.y, max.z,
                max.x, min.y, max.z,
                max.x, max.y, max.z,
                min.x, min.y, max.z,
                min.x, min.y, max.z,
                min.x, max.y, max.z,
                min.x, min.y, min.z,
                max.x, max.y, min.z,
                max.x, min.y, min.z,
                min.x, max.y, min.z,
                min.x, min.y, max.z,
                min.x, max.y, max.z,

                max.x, max.y, max.z,
                min.x, max.y, max.z,
                min.x, max.y, min.z,
                min.x, min.y, min.z,
        };

        indices = new float[]{
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 1, 18, 2, 4, 19, 5, 7, 20, 8, 10, 21, 11, 13, 22, 14, 16, 23, 17
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
        // TODO: check the photo in iphone
        // TODO: it also needs indices as we are drawing a cube
        // TODO: check the tutorial how to draw a cube
        // TODO: recalculate if rotate
        return vertices;
    }

    public float[] getIndices() {
        return indices;
    }
}
