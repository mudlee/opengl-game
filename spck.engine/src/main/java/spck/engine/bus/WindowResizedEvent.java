package spck.engine.bus;

public class WindowResizedEvent {
    public static final WindowResizedEvent reusable = new WindowResizedEvent();

    public int width;
    public int height;
}
