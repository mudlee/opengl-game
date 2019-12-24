package spck.engine.ui;

public abstract class UIElement {
	protected int x;
	protected int y;
	protected Align align;

	protected UIElement(int x, int y, Align align) {
		this.x = x;
		this.y = y;
		this.align = align;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setAlign(Align align) {
		this.align = align;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Align getAlign() {
		return align;
	}
}
