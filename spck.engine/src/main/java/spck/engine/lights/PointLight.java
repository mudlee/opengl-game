package spck.engine.lights;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class PointLight extends Light {
    private final Vector3f position;
    private final Attenuation attenuation;

    public PointLight(Vector4f color, float strength, Vector3f position, Attenuation attenuation) {
        super(color, strength);
        this.position = position;
        this.attenuation = attenuation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Attenuation getAttenuation() {
        return attenuation;
    }
}
