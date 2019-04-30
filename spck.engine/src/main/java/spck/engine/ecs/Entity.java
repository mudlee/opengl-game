package spck.engine.ecs;

import com.artemis.Component;
import com.artemis.utils.Bag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class Entity {
    private final static Logger LOGGER = LoggerFactory.getLogger(Entity.class);
    private final Bag<Component> componentBag = new Bag<>();
    private Integer id;
    private boolean destroyed;

	public static Entity create(Entity entity) {
		entity.id = ECS.world.create();
		LOGGER.trace("Entity {} [{}] is created", entity.id, entity.getClass().getSimpleName());
        entity.onEntityCreated();
		return entity;
    }

    /**
     * This method is called, when the entity is created, so components now can be added.
     */
    public abstract void onEntityCreated();

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
    public <T> T addComponent(Class<T> componentClass) {
        if (id == null || destroyed) {
            LOGGER.error("Entity {} is destroyed, cannot add component", id);
            return null;
        }

        if (!ECSComponent.class.isAssignableFrom(componentClass)) {
            throw new RuntimeException(String.format("%s is not extended from %s", componentClass, Component.class));
        }

        LOGGER.trace("Adding {} to Entity {}", componentClass.getSimpleName(), id);
        T ecsComponent = (T) ECS.world.getMapper((Class<? extends Component>) componentClass).create(id);
        ((ECSComponent) ecsComponent).entityId = id;
        return ecsComponent;
    }
}
