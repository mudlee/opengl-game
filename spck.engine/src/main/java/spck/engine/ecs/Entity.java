package spck.engine.ecs;

import com.artemis.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Entity {
    private final static Logger LOGGER = LoggerFactory.getLogger(Entity.class);
    private Integer id;
    private boolean destroyed;

    public Entity() {
        id = EntityStore.create(getClass());
        LOGGER.debug("Entity {} [{}] is created", id, getClass().getSimpleName());
    }

    public void destroy() {
        checkIfExists();
        destroyed = true;
        EntityStore.destroy(id);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getComponent(Class<T> componentClass) {
        checkIfExists();

        try {
            return Optional.of((T) ECS.world.getMapper((Class<? extends Component>) componentClass).get(id));
        } catch (ArrayIndexOutOfBoundsException ex) {
            return Optional.empty();
        }
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    protected <T> T addComponent(Class<T> componentClass) {
        checkIfExists();

        if (!Component.class.isAssignableFrom(componentClass)) {
            throw new RuntimeException(String.format("%s is not extended from %s", componentClass, Component.class));
        }

        LOGGER.debug("Adding {} to Entity {}", componentClass.getSimpleName(), id);
        return (T) ECS.world.getMapper((Class<? extends Component>) componentClass).create(id);
    }

    private void checkIfExists() {
        if (id == null || destroyed) {
            throw new RuntimeException(String.format("Entity %s is not available. Destroyed: %b", this.getClass(), destroyed));
        }
    }
}
