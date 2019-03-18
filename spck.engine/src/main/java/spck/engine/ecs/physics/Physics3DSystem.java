package spck.engine.ecs.physics;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.util.RunOnce;

// TODO remove from world, when entity is destroyed, stateawarecomponent
public class Physics3DSystem extends BaseEntitySystem {
	private final static Logger LOGGER = LoggerFactory.getLogger(Physics3DSystem.class);
	private DWorld world;
	private ComponentMapper<Physics3DBodyComponent> physics3DBodyComponents;
	private ComponentMapper<RenderComponent> renderComponents;

	public Physics3DSystem() {
		super(Aspect.all(Physics3DBodyComponent.class));
	}

	@Override
	protected void processSystem() {
		RunOnce.run("Physics3D init", () -> {
			LOGGER.debug("Creating 3D World...");
			world = OdeHelper.createWorld();
		});

		IntBag actives = subscription.getEntities();
		int[] ids = actives.getData();

		if (actives.size() == 0) {
			return;
		}

		world.step(getWorld().delta);

		for (int i = 0, s = actives.size(); s > i; i++) {
			if (physics3DBodyComponents.has(ids[i])) {
				Physics3DBodyComponent component = physics3DBodyComponents.get(ids[i]);
				if (!component.initialized) {
					initializeComponent(component);
				}

				RenderComponent renderComponent = renderComponents.get(component.entityId);
				renderComponent.transform.setPosition(
						(float) component.physicsBody.getPosition().get0(),
						(float) component.physicsBody.getPosition().get1(),
						(float) component.physicsBody.getPosition().get2()
				);
				//renderComponent.transform.setRotation(component.physicsBody.getRotation().) TODO
			}
		}
	}

	private void initializeComponent(Physics3DBodyComponent component) {
		component.initialized = true;
		component.physicsBody = OdeHelper.createBody(world);
		LOGGER.debug("Added {}-{} to Physics3D world", component.entityId, component.physicsBody.hashCode());
	}
}
