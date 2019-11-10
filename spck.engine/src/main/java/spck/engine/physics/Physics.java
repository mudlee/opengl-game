package spck.engine.physics;

import org.joml.*;
import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;

import java.util.Iterator;
import java.util.Optional;

public class Physics {
    private static Vector3f REUSABLE_MIN = new Vector3f();
    private static Vector3f REUSABLE_MAX = new Vector3f();

    public static Optional<RaycastHit> raycast(Rayf ray, float length) {
        Iterator<Entity> iterator = Entity.getAllCreated().iterator();
        Vector2f result = new Vector2f();
        Vector2f closestNearFar = null;
        Entity closest = null;

        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (!(entity instanceof PhysicsEntity)) {
                continue;
            }

            Optional<RenderComponent> renderComponentOpt = entity.getComponent(RenderComponent.class);
            if (renderComponentOpt.isPresent()) {
                RenderComponent comp = renderComponentOpt.get();
                // TODO take into account the entity's current transform changes (rotation, scale)
                for (AABBf aabb : ((PhysicsEntity) entity).getAABBs()) {
                    REUSABLE_MIN.set(
                            comp.transform.getPosition().x,
                            comp.transform.getPosition().y,
                            comp.transform.getPosition().z
                    );
                    REUSABLE_MAX.set(REUSABLE_MIN);
                    REUSABLE_MIN.add(aabb.minX, aabb.minY, aabb.minZ);
                    REUSABLE_MAX.add(aabb.maxX, aabb.maxY, aabb.maxZ);
                    AABBf transformedAABB = new AABBf(REUSABLE_MIN, REUSABLE_MAX);

                    if (Intersectionf.intersectRayAab(ray, transformedAABB, result) && result.x <= length) {
                        if (closestNearFar == null || result.x < closestNearFar.x) {
                            closestNearFar = new Vector2f(result);
                            closest = entity;
                        }
                    }
                }
            }
        }

        // TODO MOUSE PICK READ: https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/chapter23/chapter23.html
        if (closest != null) {
            // TODO: we have to calculate the hit position. We know that cam's pos and the ray and the near in the result,
            // so we have to be able to move the position on the ray from the cam and that will be the hit point
            return Optional.of(new RaycastHit(new Vector3f(), closest));
        }

        return Optional.empty();
    }
}
