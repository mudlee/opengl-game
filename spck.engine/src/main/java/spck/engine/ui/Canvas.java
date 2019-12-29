package spck.engine.ui;

import spck.engine.ecs.AbstractEntity;

import java.util.ArrayList;

public class Canvas extends AbstractEntity {
	private CanvasComponent canvasComponent;

	@Override
	protected void onEntityReady() {
		canvasComponent = addComponent(CanvasComponent.class);
	}

	public Text addText(Text text) {
		addText(text, 0);
		return text;
	}

	public Text addText(Text text, int zIndex) {
		canvasComponent.elements.putIfAbsent(zIndex, new ArrayList<>());
		canvasComponent.elements.get(zIndex).add(text);
		return text;
	}

	public Image addImage(Image image) {
		addImage(image, 0);
		return image;
	}

	public Image addImage(Image image, int zIndex) {
		canvasComponent.elements.putIfAbsent(zIndex, new ArrayList<>());
		canvasComponent.elements.get(zIndex).add(image);
		return image;
	}

	public Button addButton(Button button) {
		addButton(button, 0);
		return button;
	}

	public Button addButton(Button button, int zIndex) {
		canvasComponent.elements.putIfAbsent(zIndex, new ArrayList<>());
		canvasComponent.elements.get(zIndex).add(button);
		return button;
	}
}
