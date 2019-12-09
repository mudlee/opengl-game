package spck.game;

import org.joml.Vector2f;
import org.joml.Vector3f;
import spck.engine.Align;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UIText;
import spck.engine.framework.RGBAColor;
import spck.engine.ui.UIObjectPosition;

public class Cities extends UICanvasEntity {
    private final GameCamera camera;
    private final Vector3f point = new Vector3f(0f, 0f, 0f);
    private UIText text;

    Cities(GameCamera camera) {
        this.camera = camera;
    }

    @Override
    public void onEntityCreated() {
        super.onEntityCreated();
        text = UIText.build("Budapest", UIObjectPosition.build(10, 10, Align.TOP_LEFT)).color(RGBAColor.black());
        canvasComponent.addText(text);

        MessageBus.register(LifeCycle.UPDATE.eventID(), this::onUpdate);
    }

    private void onUpdate() {
        if (camera.isViewMatrixChanged()) {
            Vector2f pos = camera.toScreenSpace(point);
            text.setPosition(pos.x, pos.y);
        }
    }
}
