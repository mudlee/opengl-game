package spck.game.nations;

public class EuropeanUnion implements Nation {
    // https://en.wikipedia.org/wiki/List_of_cities_in_the_European_Union_by_population_within_city_limits
    private final CityArea[] areas = new CityArea[]{
            new CityArea("Berlin", new Coordinates(52.52437f, 13.41053f), 6f),
            new CityArea("Paris", new Coordinates(48.85341f, 2.3488f), 5f),
            new CityArea("Budapest", new Coordinates(47.49801f, 19.03991f), 2.5f),

    };

    @Override
    public CityArea[] getAreas() {
        return areas;
    }
}
