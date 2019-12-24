package spck.engine.ui;

import spck.engine.ecs.AbstractEntity;

public class Canvas extends AbstractEntity {
	private CanvasComponent canvasComponent;

	@Override
	protected void onEntityReady() {
		canvasComponent = addComponent(CanvasComponent.class);
	}

	public Text addText(Text text) {
		canvasComponent.texts.add(text);
		return text;
	}

	public Image addImage(Image image){
		canvasComponent.images.add(image);
		return image;
	}

	public Button addButton(Button button) {
		canvasComponent.buttons.add(button);
		return button;
	}
}
