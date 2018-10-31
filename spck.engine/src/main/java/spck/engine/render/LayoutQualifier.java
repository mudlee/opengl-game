package spck.engine.render;

public enum LayoutQualifier {
    VX_POSITION(0),
    VX_NORMAL(1),
    VX_UV_COORDS(2),
    INS_TRANSFORMATION_MATRIX_COL1(3),
    INS_TRANSFORMATION_MATRIX_COL2(4),
    INS_TRANSFORMATION_MATRIX_COL3(5),
    INS_TRANSFORMATION_MATRIX_COL4(6),
    INS_UV_SCALE(7),
    INS_UV_OFFSET(8);

    public int location;

    LayoutQualifier(int location) {
        this.location = location;
    }
}