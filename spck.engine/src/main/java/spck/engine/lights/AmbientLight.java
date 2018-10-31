package spck.engine.lights;

import org.joml.Vector4f;

public class AmbientLight extends Light {
    public AmbientLight(Vector4f color, float strength) {
        super(color, strength);
    }
}
