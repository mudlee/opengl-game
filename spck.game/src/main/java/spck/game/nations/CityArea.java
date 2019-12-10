package spck.game.nations;

import org.joml.Vector3f;

class CityArea {
    private final String name;
    private final Vector3f position;
    private final float populationMio;

    CityArea(String name, Vector3f position, float populationMio) {
        this.name = name;
        this.position = position;
        this.populationMio = populationMio;
    }

    public String getName() {
        return name;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getPopulationMio() {
        return populationMio;
    }
}
