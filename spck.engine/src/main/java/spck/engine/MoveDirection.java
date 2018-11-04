package spck.engine;

public enum MoveDirection {
    LEFT(Axis.X),
    RIGHT(Axis.X),
    FORWARD(Axis.Z),
    BACKWARD(Axis.Z),
    UPWARD(Axis.Y),
    DOWNWARD(Axis.Y);

    private Axis axis;

    MoveDirection(Axis axis) {
        this.axis = axis;
    }

    public Axis getAxis() {
        return axis;
    }
}
