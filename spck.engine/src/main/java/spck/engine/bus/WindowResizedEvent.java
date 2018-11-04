package spck.engine.bus;

public class WindowResizedEvent {
    private int width;
    private int height;

    public void set(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
