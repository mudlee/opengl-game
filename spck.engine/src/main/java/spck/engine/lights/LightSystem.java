package spck.engine.lights;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LightSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(LightSystem.class);
    public final static int MAX_NUM_LIGHTS = 10;
    private final static LightSystem instance = new LightSystem();
    private AmbientLight ambientLight;
    private List<Light> lights = new ArrayList<>();

    public static void addLight(Light light) {
        if (instance.lights.size() == 10) {
            LOGGER.warn("Max number of lights is {}", MAX_NUM_LIGHTS);
            return;
        }

        instance.lights.add(light);
    }

    public static void setAmbientLight(AmbientLight ambientLight) {
        instance.ambientLight = ambientLight;
    }

    public static Optional<AmbientLight> getAmbientLight() {
        return Optional.ofNullable(instance.ambientLight);
    }

    public static List<Light> getLights() {
        return instance.lights;
    }
}

