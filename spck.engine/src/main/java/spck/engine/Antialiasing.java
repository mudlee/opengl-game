package spck.engine;

public enum Antialiasing {
    OFF(0),
    ANTIALISING_2X(2),
    ANTIALISING_4X(4),
    ANTIALISING_8X(8),
    ANTIALISING_16X(16);

    private int value;

    Antialiasing(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
