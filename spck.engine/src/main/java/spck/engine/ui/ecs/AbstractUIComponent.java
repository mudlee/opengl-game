package spck.engine.ui.ecs;

import com.artemis.Component;
import org.joml.Vector2f;
import org.joml.Vector2i;
import spck.engine.Engine;
import spck.engine.graphics.UIObjectPosition;
import spck.engine.util.Pixel;

class AbstractUIComponent extends Component {
    UIObjectPosition position = UIObjectPosition.topLeft(0, 0);
    public Vector2i screenOffset = new Vector2i().zero();
    public Vector2f screenCoords = new Vector2f();

    public void updateScreenCoords() {
        switch (position.getAlign()) {
            case TOP_LEFT:
                screenCoords.x = Pixel.scaled(position.getLeft());
                screenCoords.y = Pixel.scaled(position.getTop());
                break;
            case TOP_RIGHT:
                screenCoords.x = Engine.window.getPreferences().getWidth() - (Pixel.scaled(position.getRight()) + Pixel.scaled(screenOffset.x));
                screenCoords.y = Pixel.scaled(position.getTop());
                break;
            case BOTTOM_LEFT:
                screenCoords.x = Pixel.scaled(position.getLeft());
                screenCoords.y = Engine.window.getPreferences().getHeight()
                        - Pixel.scaled(screenOffset.y)
                        - Pixel.scaled(position.getBottom());
                break;
            case BOTTOM_RIGHT:
                screenCoords.x = Engine.window.getPreferences().getWidth() - (Pixel.scaled(position.getRight()) + Pixel.scaled(screenOffset.x));
                screenCoords.y = Engine.window.getPreferences().getHeight()
                        - Pixel.scaled(screenOffset.y)
                        - Pixel.scaled(position.getBottom());
                break;
            case CENTER_CENTER:
                screenCoords.x = (float) Engine.window.getPreferences().getWidth() / 2 - (float) Pixel.scaled(screenOffset.x) / 2;
                screenCoords.y = (float) Engine.window.getPreferences().getHeight() / 2 - (float) Pixel.scaled(screenOffset.y) / 2;
                break;
        }
    }
}
