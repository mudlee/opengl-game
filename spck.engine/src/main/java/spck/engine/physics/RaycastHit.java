package spck.engine.physics;

import org.joml.Vector3f;
import spck.engine.ecs.AbstractEntity;

public class RaycastHit {
    private final Vector3f position;
    private final AbstractEntity entity;

    public RaycastHit(Vector3f position, AbstractEntity entity) {
        this.position = position;
        this.entity = entity;
    }

    public Vector3f getPosition() {
        return position;
    }

    public AbstractEntity getEntity() {
        return entity;
    }
}
