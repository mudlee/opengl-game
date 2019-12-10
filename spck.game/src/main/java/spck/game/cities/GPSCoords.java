package spck.game.cities;

import org.joml.Vector3f;

class GPSCoords {
    private static final Vector3f COORDS_TEMP = new Vector3f();
    private static final float PX_COORD_ASPECT_LAT = 0.11f;
    private static final float PX_COORD_ASPECT_LON = 0.79f;

    static Vector3f toWorldPos(float lat, float lon) {
        COORDS_TEMP.set(
                lat * PX_COORD_ASPECT_LAT,
                lon * PX_COORD_ASPECT_LON,
                0
        );
        return COORDS_TEMP;
    }
}
