package spck.engine.physics;

import org.joml.*;
import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;

import java.lang.Math;
import java.util.Iterator;
import java.util.Optional;

public class Physics {
    private static final Vector3f REUSABLE_1 = new Vector3f();
    private static final Vector3f REUSABLE_2 = new Vector3f();

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
                for (AABBf aabb : ((PhysicsEntity) entity).getAABBs()) {
                    AABBf transformedAABB = transform(aabb, comp.transform.getTransformationMatrix());

                    if (Intersectionf.intersectRayAab(ray, transformedAABB, result) && result.x <= length) {
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

    // TODO: remove this, when JOML 1.9.20 is out
    private static AABBf transform(AABBf source, Matrix4fc m) {
        AABBf dest = new AABBf();
        float dx = source.maxX - source.minX, dy = source.maxY - source.minY, dz = source.maxZ - source.minZ;
        float minx = Float.POSITIVE_INFINITY, miny = Float.POSITIVE_INFINITY, minz = Float.POSITIVE_INFINITY;
        float maxx = Float.NEGATIVE_INFINITY, maxy = Float.NEGATIVE_INFINITY, maxz = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 8; i++) {
            float x = source.minX + (i & 1) * dx, y = source.minY + (i >> 1 & 1) * dy, z = source.minZ + (i >> 2 & 1) * dz;
            float tx = m.m00() * x + m.m10() * y + m.m20() * z + m.m30();
            float ty = m.m01() * x + m.m11() * y + m.m21() * z + m.m31();
            float tz = m.m02() * x + m.m12() * y + m.m22() * z + m.m32();
            minx = Math.min(tx, minx);
            miny = Math.min(ty, miny);
            minz = Math.min(tz, minz);
            maxx = Math.max(tx, maxx);
            maxy = Math.max(ty, maxy);
            maxz = Math.max(tz, maxz);
        }
        dest.minX = minx;
        dest.minY = miny;
        dest.minZ = minz;
        dest.maxX = maxx;
        dest.maxY = maxy;
        dest.maxZ = maxz;
        return dest;
    }
}
