package spck.game.nations;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.ecs.AbstractEntity;
import spck.engine.framework.RGBAColor;
import spck.engine.render.camera.OrthoCamera;
import spck.engine.ui.Canvas;
import spck.engine.ui.Text;
import spck.engine.window.GLFWWindow;

public class NationsEntity extends AbstractEntity {
    private static final Logger log = LoggerFactory.getLogger(NationsEntity.class);
    private static final Vector2f TEXT_POS_TEMP = new Vector2f();
    private static final Vector3f SCREEN_SPACE_TEMP = new Vector3f();
    private final OrthoCamera camera;
    private final Canvas canvas;
    private final GLFWWindow window;
    private final Nation[] nations = new Nation[]{
            new EuropeanUnion()
    };
    private final Text[] texts;
    private final CityArea[] areas;

    public NationsEntity(OrthoCamera camera, Canvas canvas, GLFWWindow window) {
        this.camera = camera;
        this.canvas = canvas;
        this.window = window;

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
                // 0,0 is at top left
                texts[i] = Text.Builder
                        .create(area.getName())
                        .withX(window.getWindowWidth()/2 + Coordinates.longitudeToX(area.getCoordinates().getLongitude()))
                        .withY(window.getWindowHeight()/2 - Coordinates.latitudeToY(area.getCoordinates().getLatitude()))
                        .withColor(RGBAColor.red())
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
                // 0,0 is in the middle of the screen
                SCREEN_SPACE_TEMP.set(
                    window.getWindowWidth()/2f + Coordinates.longitudeToX(areas[i].getCoordinates().getLongitude()),
                    window.getWindowHeight()/2f - Coordinates.latitudeToY(areas[i].getCoordinates().getLatitude()),
                    0
                );
                TEXT_POS_TEMP.set(camera.worldSpaceToScreenSpace(SCREEN_SPACE_TEMP));
                texts[i].setX((int) TEXT_POS_TEMP.x);
                texts[i].setY((int) TEXT_POS_TEMP.y);
                System.out.println(TEXT_POS_TEMP.x);
                System.out.println(TEXT_POS_TEMP.y);
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
