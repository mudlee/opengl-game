package spck.game.nations;

import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.ecs.AbstractEntity;
import spck.engine.framework.RGBAColor;
import spck.engine.render.camera.OrthoCamera;
import spck.engine.ui.Canvas;
import spck.engine.ui.Text;

public class NationsEntity extends AbstractEntity {
    private static final Logger log = LoggerFactory.getLogger(NationsEntity.class);
    private static final Vector2f TEXT_POS_TEMP = new Vector2f();
    private final OrthoCamera camera;
    private final Canvas canvas;
    private final Nation[] nations = new Nation[]{
            new EuropeanUnion()
    };
    private final Text[] texts;
    private final CityArea[] areas;

    public NationsEntity(OrthoCamera camera, Canvas canvas) {
        this.camera = camera;
        this.canvas = canvas;

        int numberOfCities = getNumberOfAreas();
        texts = new Text[numberOfCities];
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
        int i = 0;
        for (Nation nation : nations) {
            for (CityArea area : nation.getAreas()) {
                texts[i] = Text.Builder
                        .create()
                        .withText(area.getName())
                        .withX((int) area.getPosition().x)
                        .withY((int) area.getPosition().y)
                        .withColor(RGBAColor.black())
                        .build();
                canvas.addText(texts[i]);
                i++;
                log.debug("Area {} added", area.getName());
            }
        }

        MessageBus.register(LifeCycle.UPDATE.eventID(), this::onUpdate);
    }

    private void onUpdate() {
        if (camera.isViewMatrixChanged()) {
            for (int i = 0; i < texts.length; i++) {
                TEXT_POS_TEMP.set(camera.worldSpaceToScreenSpace(areas[i].getPosition()));
                texts[i].setX((int) TEXT_POS_TEMP.x);
                texts[i].setY((int) TEXT_POS_TEMP.y);
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
