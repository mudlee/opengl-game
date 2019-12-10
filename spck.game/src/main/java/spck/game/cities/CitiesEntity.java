package spck.game.cities;

import org.joml.Vector2f;
import org.joml.Vector3f;
import spck.engine.Align;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UIText;
import spck.engine.framework.RGBAColor;
import spck.engine.ui.UIObjectPosition;
import spck.game.GameCamera;

public class CitiesEntity extends UICanvasEntity {
    private final GameCamera camera;
    private final Vector3f point = new Vector3f(0f, 0f, 0f);
    private UIText bpText;
    private UIText romeText;

    public CitiesEntity(GameCamera camera) {
        this.camera = camera;
    }

    @Override
    public void onEntityCreated() {
        super.onEntityCreated();
        bpText = UIText.build("Budapest", UIObjectPosition.build(0, 0, Align.TOP_LEFT)).color(RGBAColor.black());
        romeText = UIText.build("Rome", UIObjectPosition.build(0, 0, Align.TOP_LEFT)).color(RGBAColor.black());
        canvasComponent.addText(bpText);
        canvasComponent.addText(romeText);

        MessageBus.register(LifeCycle.UPDATE.eventID(), this::onUpdate);
    }

    private void onUpdate() {
        if (camera.isViewMatrixChanged()) {
            Vector2f bpPos = camera.toScreenSpace(GPSCoords.toWorldPos(Cities.BUDAPEST.getLat(), Cities.BUDAPEST.getLon()));
            bpText.setPosition(bpPos.x, bpPos.y);
            Vector2f romePos = camera.toScreenSpace(GPSCoords.toWorldPos(Cities.ROME.getLat(), Cities.ROME.getLon()));
            romeText.setPosition(romePos.x, romePos.y);
        }
    }
}
