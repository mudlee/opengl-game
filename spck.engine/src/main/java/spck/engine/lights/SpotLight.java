package spck.engine.lights;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class SpotLight extends Light {
    private final Vector3f position;
    private final Vector3f coneDirection;
    private final float cutOff;
    private final Attenuation attenuation;

    public SpotLight(Vector4f color, float strength, Vector3f position, Vector3f coneDirection, float cutOff, Attenuation attenuation) {
        super(color, strength);
        this.position = position;
        this.coneDirection = coneDirection;
        this.cutOff = cutOff;
        this.attenuation = attenuation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getConeDirection() {
        return coneDirection;
    }

    public float getCutOff() {
        return cutOff;
    }

    public Attenuation getAttenuation() {
        return attenuation;
    }
}
