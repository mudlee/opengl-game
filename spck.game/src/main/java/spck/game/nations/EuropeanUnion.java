package spck.game.nations;

import org.joml.Vector3f;

public class EuropeanUnion implements Nation {
    private final CityArea[] areas = new CityArea[]{
            new CityArea("Budapest", new Vector3f(5f, 15f, 0), 2.5f),
            new CityArea("Paris", new Vector3f(0.5f, 15.3f, 0), 5f),
            new CityArea("Berlin", new Vector3f(3.3f, 16.5f, 0), 6f),
    };

    @Override
    public CityArea[] getAreas() {
        return areas;
    }
}
