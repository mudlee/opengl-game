package spck.engine.physics;

import org.joml.AABBf;

import java.util.List;

public interface PhysicsEntity {
    List<AABBf> getAABBs();
}
