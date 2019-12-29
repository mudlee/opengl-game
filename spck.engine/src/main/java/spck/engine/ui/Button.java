package spck.engine.ui;

import spck.engine.Engine;
import spck.engine.framework.RGBAColor;

public class Button extends UIElement {
	public class Stroke {
		protected RGBAColor color;
		protected int strength;

		public Stroke(RGBAColor color, int strength) {
			this.color = color;
			this.strength = strength;
		}
	}

	protected String text;
	protected int textSize;
	protected Align textAlign;
	protected String textFont;
	protected RGBAColor textColor;
	protected RGBAColor textMouseOverColor;
	protected RGBAColor backgroundColor;
	protected RGBAColor backgroundMouseOverColor;
	protected int width;
	protected int height;
	protected int cornerRadius;
	protected Stroke stroke;
	protected Runnable onClickHandler;

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
			Stroke stroke,
			Runnable onClickHandler
	) {
		super(x, y, align);
		this.text = text;
		this.textSize = textSize;
		this.textAlign = textAlign;
		this.textFont = textFont;
		this.textColor = textColor;
		this.textMouseOverColor = textMouseOverColor;
		this.backgroundColor = backgroundColor;
		this.backgroundMouseOverColor = backgroundMouseOverColor;
		this.width = width;
		this.height = height;
		this.cornerRadius = cornerRadius;
		this.stroke = stroke;
		this.onClickHandler = onClickHandler;
	}

	public static final class Builder {
		private int x;
		private int y;
		private Align align = Align.TOP_LEFT;
		private String text;
		private int textSize = 15;
		private Align textAlign = Align.MIDDLE_CENTER;
		private String textFont = Engine.preferences.defaultFont;
		private RGBAColor textColor = RGBAColor.white();
		private RGBAColor textMouseOverColor;
		private RGBAColor backgroundColor = RGBAColor.black();
		private RGBAColor backgroundMouseOverColor;
		private int width;
		private int height;
		private int cornerRadius;
		private Stroke stroke;
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

		public Builder withStrore(Stroke stroke) {
			this.stroke = stroke;
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
					stroke,
					onClickHandler
			);
		}
	}
}
