package spck.engine.bus;

import spck.engine.Engine;

public class KeyEvent {
    private int key;
    private int scancode;
    private int action;
    private int mods;

    public static String pressed(int keyCode) {
        return Engine.ID + "KEY_PRESSED" + keyCode;
    }

    public static String released(int keyCode) {
        return Engine.ID + "KEY_RELEASED" + keyCode;
    }

    public static String keyHeldDown(int keyCode) {
        return Engine.ID + "KEY_HELD_DOWN" + keyCode;
    }

    public void set(int key, int scancode, int action, int mods) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.mods = mods;
    }

    public int getKey() {
        return key;
    }

    public int getScancode() {
        return scancode;
    }

    public int getAction() {
        return action;
    }

    public int getMods() {
        return mods;
    }
}
