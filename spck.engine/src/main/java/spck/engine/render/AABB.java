package spck.engine.render;

import org.joml.Vector3f;

public class AABB {
    private final Vector3f min;
    private final Vector3f max;

    public AABB(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;
    }

    public Vector3f getMin() {
        return min;
    }

    public Vector3f getMax() {
        return max;
    }
}
