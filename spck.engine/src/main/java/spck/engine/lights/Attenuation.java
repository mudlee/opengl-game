package spck.engine.lights;

/**
 * Attenuation settings come from @see http://wiki.ogre3d.org/tiki-index.php?page=-Point+Light+Attenuation
 */
public class Attenuation {
    private final float constant;
    private final float linear;
    private final float quadratic;


    public static Attenuation distance7() {
        return new Attenuation(1f, 0.7f, 1.8f);
    }

    public static Attenuation distance13() {
        return new Attenuation(1f, 0.35f, 0.44f);
    }

    public static Attenuation distance20() {
        return new Attenuation(1f, 0.22f, 0.2f);
    }

    public static Attenuation distance32() {
        return new Attenuation(1f, 0.14f, 0.07f);
    }

    public static Attenuation distance50() {
        return new Attenuation(1f, 0.09f, 0.032f);
    }

    public static Attenuation distance65() {
        return new Attenuation(1f, 0.07f, 0.017f);
    }

    public static Attenuation distance100() {
        return new Attenuation(1f, 0.045f, 0.0075f);
    }

    public static Attenuation distance160() {
        return new Attenuation(1f, 0.027f, 0.0028f);
    }

    public static Attenuation distance200() {
        return new Attenuation(1f, 0.022f, 0.0019f);
    }

    public static Attenuation distance325() {
        return new Attenuation(1f, 0.014f, 0.0007f);
    }

    public static Attenuation distance600() {
        return new Attenuation(1f, 0.007f, 0.0002f);
    }

    public static Attenuation distance3250() {
        return new Attenuation(1f, 0.0014f, 0.000007f);
    }

    public Attenuation(float constant, float linear, float quadratic) {
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    public float getConstant() {
        return constant;
    }

    public float getLinear() {
        return linear;
    }

    public float getQuadratic() {
        return quadratic;
    }
}
