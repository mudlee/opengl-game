package spck.engine.ecs;

import com.artemis.Component;
import com.artemis.utils.Bag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class AbstractEntity {
	private final static Logger log = LoggerFactory.getLogger(AbstractEntity.class);
	private final Bag<Component> componentBag = new Bag<>();
	private Integer id;
	private boolean destroyed;

	protected abstract void onEntityReady();

	void entityCreated(int id) {
		this.id = id;
		this.onEntityReady();
	}

	@SuppressWarnings({"unchecked", "SameParameterValue"})
	public <T> T addComponent(Class<T> componentClass) {
		if (id == null || destroyed) {
			log.error("Entity {} is destroyed, cannot add component", id);
			return null;
		}

		if (!ECSComponent.class.isAssignableFrom(componentClass)) {
			throw new RuntimeException(String.format("%s is not extended from %s", componentClass, Component.class));
		}

		log.debug("Adding {} to Entity {}", componentClass.getSimpleName(), id);
		T ecsComponent = (T) ECS.getWorld().getMapper((Class<? extends Component>) componentClass).create(id);
		((ECSComponent) ecsComponent).entityId = id;
		return ecsComponent;
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> getComponent(Class<T> componentClass) {
		if (id == null || destroyed) {
			log.error("Entity {} is destroyed, cannot get component", id);
			return Optional.empty();
		}

		try {
			return Optional.of((T) ECS.getWorld().getMapper((Class<? extends Component>) componentClass).get(id));
		} catch (ArrayIndexOutOfBoundsException ex) {
			return Optional.empty();
		}
	}

	public void destroy() {
		if (id == null || destroyed) {
			log.error("Entity {} is already destroyed", id);
			return;
		}

		destroyed = true;

		componentBag.clear();
		ECS.getWorld().getComponentManager().getComponentsFor(id, componentBag);
		for (Component component : componentBag) {
			if (component instanceof StateAwareComponent) {
				((StateAwareComponent) component).state = ComponentState.MARKED_FOR_DESTROY;
				log.debug("Entity {} is marked for deletion", id);
			}
		}
		componentBag.clear();
	}
}
