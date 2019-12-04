package spck.game;

import org.joml.AABBf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import spck.engine.MoveDirection;
import spck.engine.ecs.Entity;
import spck.engine.ecs.render.components.RenderComponent;
import spck.engine.ecs.ui.UIImage;
import spck.engine.model.ModelLoader;
import spck.engine.physics.PhysicsEntity;
import spck.engine.render.MeshMaterialCollection;

import java.util.List;

public class Map extends Entity implements PhysicsEntity {
    private static final float MOVE_SPEED = 100f;

    private final GameCamera camera;
    private UIImage image;
    private final Vector2f moveTarget;
    private static final Vector2f REUSABLE_2D_VECTOR = new Vector2f();
    private static final float ACCELERATION = 3f;
    private MeshMaterialCollection collection;

    public Map(GameCamera camera) {
        this.camera = camera;
        moveTarget = new Vector2f(camera.getPosition().x, camera.getPosition().y);
    }

    @Override
    public void onEntityCreated() {
        collection = ModelLoader.load("/models/environment/world.obj");

        RenderComponent component = addComponent(RenderComponent.class);
        component.meshMaterialCollection = collection;
        component.transform.setRotation(new Vector3f(90, 0, 0));
        component.transform.setScale(new Vector3f(10, 1, 10));

        /*Input.onMouseMove(event -> {
            move(event.direction);
        });*/

        /*Input.onMouseScroll(event -> {
            if (event.offset.y > 0) {
            } else {
            }
        });*/

        /*MessageBus.register(LifeCycle.UPDATE.eventID(), () -> {
            float distance = moveTarget.distance(image.getPosition().get().x, image.getPosition().get().y);
            if (distance > 0.01f) {
                REUSABLE_2D_VECTOR.set(image.getPosition().get().x, image.getPosition().get().y);
                REUSABLE_2D_VECTOR.lerp(moveTarget, Time.deltaTime * ACCELERATION);
                image.setPosition(REUSABLE_2D_VECTOR.x, REUSABLE_2D_VECTOR.y);
            }
        });*/
    }

    private void move(MoveDirection direction) {
        switch (direction) {
            case LEFT:
                moveTarget.set(image.getPosition().get().x + MOVE_SPEED, image.getPosition().get().y);
                break;
            case RIGHT:
                moveTarget.set(image.getPosition().get().x - MOVE_SPEED, image.getPosition().get().y);
                break;
            case UPWARD:
                moveTarget.set(image.getPosition().get().x, image.getPosition().get().y + MOVE_SPEED);
                break;
            case DOWNWARD:
                moveTarget.set(image.getPosition().get().x, image.getPosition().get().y - MOVE_SPEED);
                break;
        }
    }

    @Override
    public List<AABBf> getAABBs() {
        return collection.getAABBs();
    }
}
