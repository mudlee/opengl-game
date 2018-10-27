package spck.engine.bus;

public enum LifeCycle {
    START,
    UPDATE,
    CLEANUP,
    WINDOW_RESIZED;

    public String eventID() {
        return "LIFECYCLE" + name();
    }
}
