package spck.game.nations;

// https://en.wikipedia.org/wiki/Mercator_projection
public class Coordinates {
    private final float latitude;
    private final float longitude;
    private static final float R = (float)2058*2f / (float) Math.PI / 2f;;

    public Coordinates(float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static int longitudeToX(float longitude) {
        return (int) (0.99f * R * Math.toRadians(longitude));
    }

    public static int latitudeToY(float latitude) {
        return (int) (0.99f * R * Math.log(Math.tan(Math.PI / 4f + Math.toRadians(latitude/2f))));
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }
}
