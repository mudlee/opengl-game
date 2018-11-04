package spck.engine.bus;

public enum LifeCycle {
    GAME_START,
    FRAME_START,
    UPDATE,
    AFTER_UPDATE,
    BEFORE_FRAME_SYNC,
    AFTER_FRAME_SYNC,
    BEFORE_BUFFER_SWAP,
    AFTER_BUFFER_SWAP,
    CLEANUP,
    WINDOW_RESIZED;

    public String eventID() {
        return "LIFECYCLE" + name();
    }
}
