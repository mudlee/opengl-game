package spck.engine.ui;

import spck.engine.framework.RGBAColor;

import java.util.Optional;

public class Text extends UIElement {
	private String text;
	private int size;
	private String font;
	private RGBAColor color;

	private Text(String text, int x, int y, Align align) {
		super(x, y, align);
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public int getSize() {
		return size;
	}

	public Optional<String> getFont() {
		return Optional.ofNullable(font);
	}

	public Optional<RGBAColor> getColor() {
		return Optional.ofNullable(color);
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
		private String text;
		private int size = 15;
		private String font;
		private RGBAColor color;

		private Builder() {
		}

		public static Builder create(String text) {
			Builder builder = new Builder();
			builder.text = text;
			return builder;
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
			Text text = new Text(this.text, x, y, align);
			text.font = this.font;
			text.size = this.size;
			text.color = this.color;
			return text;
		}
	}
}
