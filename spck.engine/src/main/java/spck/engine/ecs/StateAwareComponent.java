package spck.engine.ecs;

import com.artemis.Component;

public abstract class StateAwareComponent extends Component {
    public ComponentState state = ComponentState.ACTIVE;
}
