package spck.engine.ui;

import spck.engine.Engine;
import spck.engine.framework.RGBAColor;

public class Text extends UIElement {
	private String text;
	private int size;
	private String font;
	private RGBAColor color;

	private Text(int x, int y, Align align) {
		super(x, y, align);
	}

	public String getText() {
		return text;
	}

	public int getSize() {
		return size;
	}

	public String getFont() {
		return font;
	}

	public RGBAColor getColor() {
		return color;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setFont(String font) {
		this.font = font;
	}

	public void setColor(RGBAColor color) {
		this.color = color;
	}

	public static final class Builder {
		private int x;
		private int y;
		private Align align = Align.TOP_LEFT;
		private String text = "TEXT";
		private int size = 15;
		private String font = Engine.preferences.defaultFont;
		private RGBAColor color = RGBAColor.white();

		private Builder() {
		}

		public static Builder create() {
			return new Builder();
		}

		public Builder withText(String text) {
			this.text = text;
			return this;
		}

		public Builder withX(int x) {
			this.x = x;
			return this;
		}

		public Builder withY(int y) {
			this.y = y;
			return this;
		}

		public Builder withAlign(Align align) {
			this.align = align;
			return this;
		}

		public Builder withSize(int size) {
			this.size = size;
			return this;
		}

		public Builder withFont(String font) {
			this.font = font;
			return this;
		}

		public Builder withColor(RGBAColor color) {
			this.color = color;
			return this;
		}

		public Text build() {
			Text text = new Text(x, y, align);
			text.text = this.text;
			text.font = this.font;
			text.size = this.size;
			text.color = this.color;
			return text;
		}
	}
}
