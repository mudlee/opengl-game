package spck.engine.ecs.physics;

import org.ode4j.ode.DBody;
import spck.engine.ecs.ECSComponent;

public class Physics3DBodyComponent extends ECSComponent {
	public boolean initialized;
	public DBody physicsBody;
}
