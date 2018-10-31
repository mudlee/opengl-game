package spck.engine.ecs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EntityStore {
    private final static Logger LOGGER = LoggerFactory.getLogger(EntityStore.class);
    private final static List<Integer> waitingForDestroy = new ArrayList<>();

    public static int create(Class clazz) {
        int id = ECS.world.create();
        LOGGER.debug("Entity {} [{}] is created", id, clazz.getSimpleName());
        return id;
    }

    public static void destroy(int id) {
        LOGGER.debug("Entity {} is marked for deletion", id);
        waitingForDestroy.add(id);
    }

    public static List<Integer> getWaitingForDestroy() {
        return waitingForDestroy;
    }
}
