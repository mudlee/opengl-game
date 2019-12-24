package spck.engine.ui;

public class Image extends UIElement {
	private int textureId;
	private int width;
	private int height;
	protected Integer handle;

	public Image(int x, int y, Align align, int textureId, int width, int height) {
		super(x, y, align);
		this.textureId = textureId;
		this.width = width;
		this.height = height;
	}

	public void setTextureId(int textureId) {
		this.textureId = textureId;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getTextureId() {
		return textureId;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}


	public static final class Builder {
		protected int x;
		protected int y;
		protected Align align = Align.TOP_LEFT;
		protected Integer handle;
		private int textureId;
		private int width;
		private int height;

		private Builder() {
		}

		public static Builder create(int textureId) {
			Builder builder = new Builder();
			builder.textureId = textureId;
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

		public Builder withWidth(int width) {
			this.width = width;
			return this;
		}

		public Builder withAlign(Align align) {
			this.align = align;
			return this;
		}

		public Builder withHeight(int height) {
			this.height = height;
			return this;
		}

		public Builder withHandle(Integer handle) {
			this.handle = handle;
			return this;
		}

		public Image build() {
			Image image = new Image(x, y, align, textureId, width, height);
			image.handle = this.handle;
			return image;
		}
	}
}
