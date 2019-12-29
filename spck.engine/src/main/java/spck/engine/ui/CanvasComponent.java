package spck.engine.ui;

import spck.engine.ecs.ECSComponent;

import java.util.HashMap;
import java.util.List;

public class CanvasComponent extends ECSComponent {
	public final HashMap<Integer, List<UIElement>> elements = new HashMap<>();
}
