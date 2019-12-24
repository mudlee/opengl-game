package spck.game.nations;

import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.Align;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.ecs.ui.UICanvasEntity;
import spck.engine.ecs.ui.UIText;
import spck.engine.framework.RGBAColor;
import spck.engine.render.camera.OrthoCamera;
import spck.engine.ui.UIObjectPosition;
import spck.engine.window.GLFWWindow;

public class NationsEntity extends UICanvasEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(NationsEntity.class);
    private static final Vector2f TEXT_POS_TEMP = new Vector2f();
    private final OrthoCamera camera;
    private final GLFWWindow window;
    private final Nation[] nations = new Nation[]{
            new EuropeanUnion()
    };
    private final UIText[] texts;
    private final CityArea[] areas;

    public NationsEntity(OrthoCamera camera, GLFWWindow window) {
        this.camera = camera;
        this.window = window;

        int numberOfCities = getNumberOfAreas();
        texts = new UIText[numberOfCities];
        areas = new CityArea[numberOfCities];

        int i = 0;
        for (Nation nation : nations) {
            for (CityArea area : nation.getAreas()) {
                areas[i++] = area;
            }
        }
    }

    @Override
    public void onEntityReady() {
        super.onEntityReady();

        int i = 0;
        for (Nation nation : nations) {
            for (CityArea area : nation.getAreas()) {
                texts[i] = UIText.build(
                        area.getName(),
                        UIObjectPosition.build((int) area.getPosition().x, (int) area.getPosition().y, Align.TOP_LEFT),
                        window
                ).color(RGBAColor.black());
                canvasComponent.addText(texts[i]);
                i++;
                LOGGER.debug("Area {} added", area.getName());
            }
        }

        MessageBus.register(LifeCycle.UPDATE.eventID(), this::onUpdate);
    }

    private void onUpdate() {
        if (camera.isViewMatrixChanged()) {
            for (int i = 0; i < texts.length; i++) {
                TEXT_POS_TEMP.set(camera.worldSpaceToScreenSpace(areas[i].getPosition()));
                texts[i].setPosition(TEXT_POS_TEMP.x, TEXT_POS_TEMP.y);
            }
        }
    }

    private int getNumberOfAreas() {
        int count = 0;
        for (Nation nation : nations) {
            count += nation.getAreas().length;
        }
        return count;
    }
}
