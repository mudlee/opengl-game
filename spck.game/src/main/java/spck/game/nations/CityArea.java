package spck.game.nations;

class CityArea {
    private final String name;
    private final Coordinates coordinates;
    private final float populationMio;

    CityArea(String name, Coordinates coordinates, float populationMio) {
        this.name = name;
        this.coordinates = coordinates;
        this.populationMio = populationMio;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public float getPopulationMio() {
        return populationMio;
    }
}
