package spck.game.ui.debug;

import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.ecs.Entity;

public class StatusUI {
    public StatusUI() {
        MessageBus.register(LifeCycle.GAME_START.eventID(), this::onStart);
    }

    private void onStart() {
        StatusUICanvasEntity canvas = new StatusUICanvasEntity();
        Entity.create(canvas);
    }
}
