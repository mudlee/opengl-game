package spck.engine.graphics;

public class UIObjectPosition {
    private int top = 0;
    private int bottom = 0;
    private int left = 0;
    private int right = 0;
    private Align align;

    private UIObjectPosition() {
    }

    @Override
    public String toString() {
        return "UIObjectPosition{" +
                "top=" + top +
                ", bottom=" + bottom +
                ", left=" + left +
                ", right=" + right +
                ", align=" + align +
                '}';
    }

    public static UIObjectPosition centerCenter(int top, int left) {
        UIObjectPosition position = new UIObjectPosition();
        position.align = Align.CENTER_CENTER;
        position.top = top;
        position.left = left;
        return position;
    }

    public static UIObjectPosition topLeft(int top, int left) {
        UIObjectPosition position = new UIObjectPosition();
        position.align = Align.TOP_LEFT;
        position.top = top;
        position.left = left;
        return position;
    }

    public static UIObjectPosition topRight(int top, int right) {
        UIObjectPosition position = new UIObjectPosition();
        position.align = Align.TOP_RIGHT;
        position.top = top;
        position.right = right;
        return position;
    }

    public static UIObjectPosition bottomLeft(int bottom, int left) {
        UIObjectPosition position = new UIObjectPosition();
        position.align = Align.BOTTOM_LEFT;
        position.bottom = bottom;
        position.left = left;
        return position;
    }

    public static UIObjectPosition bottomRight(int bottom, int right) {
        UIObjectPosition position = new UIObjectPosition();
        position.align = Align.BOTTOM_RIGHT;
        position.bottom = bottom;
        position.right = right;
        return position;
    }

    public int getTop() {
        return top;
    }

    public UIObjectPosition setTop(int top) {
        this.top = top;
        return this;
    }

    public int getBottom() {
        return bottom;
    }

    public UIObjectPosition setBottom(int bottom) {
        this.bottom = bottom;
        return this;
    }

    public int getLeft() {
        return left;
    }

    public UIObjectPosition setLeft(int left) {
        this.left = left;
        return this;
    }

    public int getRight() {
        return right;
    }

    public UIObjectPosition setRight(int right) {
        this.right = right;
        return this;
    }

    public Align getAlign() {
        return align;
    }

    public void setAlign(Align align) {
        this.align = align;
    }

    public UIObjectPosition set(UIObjectPosition position) {
        left = position.left;
        right = position.right;
        top = position.top;
        bottom = position.bottom;
        align = position.align;

        return this;
    }
}
