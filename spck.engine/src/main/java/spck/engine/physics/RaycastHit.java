package spck.engine.physics;

import org.joml.Vector3f;
import spck.engine.ecs.Entity;

public class RaycastHit {
    private final Vector3f position;
    private final Entity entity;

    public RaycastHit(Vector3f position, Entity entity) {
        this.position = position;
        this.entity = entity;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Entity getEntity() {
        return entity;
    }
}
