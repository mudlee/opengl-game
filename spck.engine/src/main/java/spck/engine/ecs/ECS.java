package spck.engine.ecs;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.Time;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;

import java.util.List;

public class ECS {
    public static World world;
    private final static Logger LOGGER = LoggerFactory.getLogger(ECS.class);

    public ECS(List<BaseSystem> systems) {
        LOGGER.debug("Initialising ECS World...");
        WorldConfigurationBuilder builder = new WorldConfigurationBuilder();

        for (BaseSystem system : systems) {
            LOGGER.debug("    Registering {} system...", system.getClass());
            builder = builder.with(system);
        }

        world = new World(builder.build());
        MessageBus.register(LifeCycle.AFTER_UPDATE.eventID(), this::process);
        LOGGER.debug("ECS is ready");
    }

    private void process() {
        world.setDelta(Time.deltaTime);
        world.process();
    }
}
