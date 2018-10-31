package spck.engine.lights;

import org.joml.Vector4f;

public abstract class Light {
    public enum Type {
        DIRECTIONAL(0),
        POINT(1),
        SPOT(2);

        private int shaderCode;

        Type(int shaderCode) {
            this.shaderCode = shaderCode;
        }

        public int getShaderCode() {
            return shaderCode;
        }
    }

    private final Vector4f color;
    private final float strength;
    private boolean changed;

    Light(Vector4f color, float strength) {
        this.color = color;
        this.strength = strength;
        changed = true;
    }

    public boolean isChanged() {
        return changed;
    }

    public void ackChange() {
        changed = false;
    }

    public Vector4f getColor() {
        return color;
    }

    public float getStrength() {
        return strength;
    }
}
