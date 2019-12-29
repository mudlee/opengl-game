package spck.engine;

import org.joml.Vector4f;

public class EnginePreferences {
	protected String defaultFont;
	protected boolean polygonRenderMode;
	protected boolean renderAABB;
	protected Vector4f clearColor;

	private EnginePreferences(String defaultFont, boolean polygonRenderMode, boolean renderAABB, Vector4f clearColor) {
		this.defaultFont = defaultFont;
		this.polygonRenderMode = polygonRenderMode;
		this.renderAABB = renderAABB;
		this.clearColor = clearColor;
	}

	public static final class Builder {
		private String defaultFont = "GeosansLight";
		private boolean polygonRenderMode;
		private boolean renderAABB;
		private Vector4f clearColor = new Vector4f(1f, 1f, 1f, 0f);

		private Builder() {
		}

		public static Builder create() {
			return new Builder();
		}

		public Builder withDefaultFont(String defaultFont) {
			this.defaultFont = defaultFont;
			return this;
		}

		public Builder withPolygonRenderMode(boolean polygonRenderMode) {
			this.polygonRenderMode = polygonRenderMode;
			return this;
		}

		public Builder withRenderAABB(boolean renderAABB) {
			this.renderAABB = renderAABB;
			return this;
		}

		public Builder withClearColor(Vector4f clearColor) {
			this.clearColor = clearColor;
			return this;
		}

		public EnginePreferences build() {
			return new EnginePreferences(defaultFont, polygonRenderMode, renderAABB, clearColor);
		}
	}
}
