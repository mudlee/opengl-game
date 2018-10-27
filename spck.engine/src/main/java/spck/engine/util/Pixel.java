package spck.engine.util;

import spck.engine.Engine;

public class Pixel {
    public static int scaled(int pixel) {
        return pixel * Engine.window.getPreferences().getScreenScaleFactor();
    }

    public static float scaled(float pixel) {
        return pixel * (float) Engine.window.getPreferences().getScreenScaleFactor();
    }
}
