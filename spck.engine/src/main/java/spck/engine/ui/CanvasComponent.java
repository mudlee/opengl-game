package spck.engine.ui;

import spck.engine.ecs.ECSComponent;

import java.util.ArrayList;
import java.util.List;

public class CanvasComponent extends ECSComponent {
	public final List<Text> texts = new ArrayList<>();
	public final List<Image> images = new ArrayList<>();
	public final List<Button> buttons = new ArrayList<>();
}
