package spck.engine.util;

import spck.engine.Engine;

public class ScreenScale {
    public static int applyScaleFactor(int pixel) {
        return pixel * Engine.window.getPreferences().getScreenScaleFactor();
    }

    public static float applyScaleFactor(float pixel) {
        return pixel * (float) Engine.window.getPreferences().getScreenScaleFactor();
    }
}
