package spck.engine.bus;

public class KeyEvent {
    private int key;
    private int scancode;
    private int action;
    private int mods;

    public static String pressed(int keyCode) {
        return "PRESSED" + keyCode;
    }

    public static String released(int keyCode) {
        return "RELEASED" + keyCode;
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
