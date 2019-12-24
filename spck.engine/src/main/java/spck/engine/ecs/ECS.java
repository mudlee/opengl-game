package spck.engine.ecs;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.Time;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ECS {
    private static final Map<Integer, AbstractEntity> entities = new HashMap<>();
    private static World world;
    private final static Logger log = LoggerFactory.getLogger(ECS.class);
    private final WorldConfigurationBuilder builder;

    public ECS() {
        builder = new WorldConfigurationBuilder();
    }

    public static Collection<AbstractEntity> getAllCreated() {
        return entities.values();
    }

    public void add(BaseSystem system) {
        log.debug("Registering {} system...", system.getClass());
        builder.with(system);
    }

    public void createWorld() {
        log.debug("Creating ECS world...");
        if (world != null) {
            throw new RuntimeException("ECS world is already created");
        }
        world = new World(builder.build());
        MessageBus.register(LifeCycle.AFTER_UPDATE.eventID(), this::process);
        log.debug("ECS is ready");
    }

    public void createEntity(AbstractEntity entity) {
        int id = world.create();
        log.debug("Entity {} [{}] is created", id, entity.getClass().getSimpleName());
        entities.put(id, entity);
        entity.entityCreated(id);
    }

    public void destroyEntity(int id) {
        if (entities.containsKey(id)) {
            entities.get(id).destroy();
            entities.remove(id);
        }
    }

    public static World getWorld() {
        if (world == null) {
            throw new RuntimeException("ECS is not yet initiated");
        }
        return world;
    }

    private void process() {
        world.setDelta(Time.deltaTime);
        world.process();
    }
}
