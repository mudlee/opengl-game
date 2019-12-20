package spck.engine.window;

public class MouseButtonEvent {
    private int button;
    private int action;
    private int mods;

    public void set(int button, int action, int mods) {
        this.button = button;
        this.action = action;
        this.mods = mods;
    }

    public int getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public int getMods() {
        return mods;
    }
}
