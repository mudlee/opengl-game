package spck.engine.ecs.physics;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.util.RunOnce;

public class Physics3DSystem extends BaseEntitySystem {
	private final static Logger LOGGER = LoggerFactory.getLogger(Physics3DSystem.class);
	private DWorld world;

	public Physics3DSystem() {
		super(Aspect.all(Physics3DBodyComponent.class));
	}

	@Override
	protected void processSystem() {
		RunOnce.run("Physics3D init", () -> {
			LOGGER.debug("Creating 3D World...");
			world = OdeHelper.createWorld();
		});
	}
}
