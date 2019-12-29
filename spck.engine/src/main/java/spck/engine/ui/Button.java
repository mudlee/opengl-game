package spck.engine.ui;

import spck.engine.framework.RGBAColor;

import java.util.Optional;

public class Button extends UIElement {
	private String text;
	private int textSize;
	private Align textAlign;
	private String textFont;
	private RGBAColor textColor;
	private RGBAColor textMouseOverColor;
	private RGBAColor backgroundColor;
	private RGBAColor backgroundMouseOverColor;
	private int width;
	private int height;
	private int cornerRadius;
	private Runnable onClickHandler;

	public Button(
			int x,
			int y,
			Align align,
			String text,
			int textSize,
			Align textAlign,
			String textFont,
			RGBAColor textColor,
			RGBAColor textMouseOverColor,
			RGBAColor backgroundColor,
			RGBAColor backgroundMouseOverColor,
			int width,
			int height,
			int cornerRadius,
			Runnable onClickHandler
	) {
		super(x, y, align);
		this.text = text;
		this.textSize = textSize;
		this.textAlign = textAlign;
		this.textColor = textColor;
		this.textMouseOverColor = textMouseOverColor;
		this.backgroundColor = backgroundColor;
		this.backgroundMouseOverColor = backgroundMouseOverColor;
		this.width = width;
		this.height = height;
		this.cornerRadius = cornerRadius;
		this.onClickHandler = onClickHandler;
	}

	public String getText() {
		return text;
	}

	public int getTextSize() {
		return textSize;
	}

	public Align getTextAlign() {
		return textAlign;
	}

	public Optional<String> getTextFont() {
		return Optional.ofNullable(textFont);
	}

	public Optional<RGBAColor> getTextColor() {
		return Optional.ofNullable(textColor);
	}

	public Optional<RGBAColor> getTextMouseOverColor() {
		return Optional.ofNullable(textMouseOverColor);
	}

	public Optional<RGBAColor> getBackgroundColor() {
		return Optional.ofNullable(backgroundColor);
	}

	public Optional<RGBAColor> getBackgroundMouseOverColor() {
		return Optional.ofNullable(backgroundMouseOverColor);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getCornerRadius() {
		return cornerRadius;
	}

	public Optional<Runnable> getOnClickHandler() {
		return Optional.ofNullable(onClickHandler);
	}

	public static final class Builder {
		private int x;
		private int y;
		private Align align = Align.TOP_LEFT;
		private String text;
		private int textSize = 15;
		private Align textAlign = Align.MIDDLE_CENTER;
		private String textFont;
		private RGBAColor textColor;
		private RGBAColor textMouseOverColor;
		private RGBAColor backgroundColor;
		private RGBAColor backgroundMouseOverColor;
		private int width;
		private int height;
		private int cornerRadius;
		private Runnable onClickHandler;

		private Builder() {
		}

		public static Builder create(int width, int height, String text) {
			Builder builder = new Builder();
			builder.width =width;
			builder.height =height;
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

		public Builder withTextSize(int textSize) {
			this.textSize = textSize;
			return this;
		}

		public Builder withTextAlign(Align textAlign) {
			this.textAlign = textAlign;
			return this;
		}

		public Builder withTextFont(String textFont) {
			this.textFont = textFont;
			return this;
		}

		public Builder withTextColor(RGBAColor textColor) {
			this.textColor = textColor;
			return this;
		}

		public Builder withBackgroundColor(RGBAColor backgroundColor) {
			this.backgroundColor = backgroundColor;
			return this;
		}

		public Builder withTextMouseOverColor(RGBAColor textMouseOverColor) {
			this.textMouseOverColor = textMouseOverColor;
			return this;
		}

		public Builder withBackgroundMouseOverColor(RGBAColor backgroundMouseOverColor) {
			this.backgroundMouseOverColor = backgroundMouseOverColor;
			return this;
		}

		public Builder withCornerRadius(int cornerRadius) {
			this.cornerRadius = cornerRadius;
			return this;
		}

		public Builder withOnClickHandler(Runnable onClickHandler){
			this.onClickHandler = onClickHandler;
			return this;
		}

		public Button build() {
			return new Button(
					x,
					y,
					align,
					text,
					textSize,
					textAlign,
					textFont,
					textColor,
					textMouseOverColor,
					backgroundColor,
					backgroundMouseOverColor,
					width,
					height,
					cornerRadius,
					onClickHandler
			);
		}
	}
}
