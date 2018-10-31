package spck.engine.lights;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class DirectionalLight extends Light {
    private final Vector3f direction;

    public DirectionalLight(Vector4f color, float strength, Vector3f direction) {
        super(color, strength);
        this.direction = direction;
    }

    public Vector3f getDirection() {
        return direction;
    }
}
