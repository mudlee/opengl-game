package spck.engine.ecs;

public abstract class StateAwareComponent extends ECSComponent {
    public ComponentState state = ComponentState.ACTIVE;
}
