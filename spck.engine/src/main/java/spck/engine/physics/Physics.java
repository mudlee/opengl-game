package spck.engine.physics;

import org.joml.*;
import spck.engine.ecs.AbstractEntity;
import spck.engine.ecs.ECS;
import spck.engine.render.RenderComponent;

import java.util.Iterator;
import java.util.Optional;

public class Physics {
    private static final Vector3f REUSABLE_1 = new Vector3f();
    private static final Vector3f REUSABLE_2 = new Vector3f();
    private static final AABBf REUSABLE_AABB = new AABBf();

    public static Optional<RaycastHit> raycast(Rayf ray, float length) {
        Iterator<AbstractEntity> iterator = ECS.getAllCreated().iterator();
        Vector2f result = new Vector2f();
        Vector2f closestNearFar = null;
        AbstractEntity closest = null;

        while (iterator.hasNext()) {
            AbstractEntity entity = iterator.next();
            if (!(entity instanceof PhysicsEntity)) {
                continue;
            }

            Optional<RenderComponent> renderComponentOpt = entity.getComponent(RenderComponent.class);
            if (renderComponentOpt.isPresent()) {
                RenderComponent comp = renderComponentOpt.get();
                for (AABBf aabb : ((PhysicsEntity) entity).getAABBs()) {
                    aabb.transform(comp.transform.getTransformationMatrix(), REUSABLE_AABB);

                    if (Intersectionf.intersectRayAab(ray, REUSABLE_AABB, result) && result.x <= length) {
                        if (closestNearFar == null) {
                            closestNearFar = new Vector2f(result);
                            closest = entity;
                        } else if (result.x < closestNearFar.x) {
                            closestNearFar.set(result);
                            closest = entity;
                        }
                    }
                }
            }
        }

        if (closest != null) {
            REUSABLE_1.set(ray.oX, ray.oY, ray.oZ);
            REUSABLE_2.set(ray.dX, ray.dY, ray.dZ);
            REUSABLE_2.mul(closestNearFar.x);
            REUSABLE_1.add(REUSABLE_2);
            return Optional.of(new RaycastHit(REUSABLE_1, closest));
        }

        return Optional.empty();
    }
}
