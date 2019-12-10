package spck.game.cities;

public enum Cities {
    BUDAPEST(47.4979f, 19.0402f, "Budapest"),
    ROME(41.9015f, 12.4608f, "Rome");

    private float lat;
    private float lon;
    private String name;

    Cities(float lat, float lon, String name) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    public String getName() {
        return name;
    }
}
