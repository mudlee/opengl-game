package spck.engine.ui;

import static org.lwjgl.nanovg.NanoVG.*;

public enum Align {
	TOP_LEFT(NVG_ALIGN_TOP | NVG_ALIGN_LEFT),
	TOP_RIGHT(NVG_ALIGN_TOP | NVG_ALIGN_RIGHT),
	TOP_CENTER(NVG_ALIGN_TOP | NVG_ALIGN_CENTER),
	MIDDLE_LEFT(NVG_ALIGN_MIDDLE | NVG_ALIGN_LEFT),
	MIDDLE_RIGHT(NVG_ALIGN_MIDDLE | NVG_ALIGN_RIGHT),
	MIDDLE_CENTER(NVG_ALIGN_MIDDLE | NVG_ALIGN_CENTER),
	BOTTOM_LEFT(NVG_ALIGN_BOTTOM | NVG_ALIGN_LEFT),
	BOTTOM_RIGHT(NVG_ALIGN_BOTTOM | NVG_ALIGN_RIGHT),
	BOTTOM_CENTER(NVG_ALIGN_BOTTOM | NVG_ALIGN_CENTER);

	private int nvgValue;

	Align(int nvgValue) {
		this.nvgValue = nvgValue;
	}

	public int getNvgValue() {
		return nvgValue;
	}
}
