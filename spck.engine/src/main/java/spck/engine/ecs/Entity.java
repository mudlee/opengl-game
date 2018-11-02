package spck.engine.ecs;

import com.artemis.Component;
import com.artemis.utils.Bag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Entity {
    private final static Logger LOGGER = LoggerFactory.getLogger(Entity.class);
    private final Bag<Component> componentBag = new Bag<>();
    private Integer id;
    private boolean destroyed;

    public Entity() {
        id = ECS.world.create();
        LOGGER.trace("Entity {} [{}] is created", id, getClass().getSimpleName());
    }

    public void destroy() {
        if (id == null || destroyed) {
            LOGGER.error("Entity {} is already destroyed", id);
            return;
        }

        destroyed = true;

        componentBag.clear();
        ECS.world.getComponentManager().getComponentsFor(id, componentBag);
        for (Component component : componentBag) {
            if (component instanceof StateAwareComponent) {
                ((StateAwareComponent) component).state = ComponentState.MARKED_FOR_DESTROY;
                LOGGER.trace("Entity {} is marked for deletion", id);
            }
        }
        componentBag.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getComponent(Class<T> componentClass) {
        if (id == null || destroyed) {
            LOGGER.error("Entity {} is destroyed, cannot get component", id);
            return Optional.empty();
        }

        try {
            return Optional.of((T) ECS.world.getMapper((Class<? extends Component>) componentClass).get(id));
        } catch (ArrayIndexOutOfBoundsException ex) {
            return Optional.empty();
        }
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    protected <T> T addComponent(Class<T> componentClass) {
        if (id == null || destroyed) {
            LOGGER.error("Entity {} is destroyed, cannot add component", id);
            return null;
        }

        if (!Component.class.isAssignableFrom(componentClass)) {
            throw new RuntimeException(String.format("%s is not extended from %s", componentClass, Component.class));
        }

        LOGGER.trace("Adding {} to Entity {}", componentClass.getSimpleName(), id);
        return (T) ECS.world.getMapper((Class<? extends Component>) componentClass).create(id);
    }
}
