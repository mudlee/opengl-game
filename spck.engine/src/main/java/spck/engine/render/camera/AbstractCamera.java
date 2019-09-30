package spck.engine.render.camera;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Rayf;
import org.joml.Vector3f;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;

public class AbstractCamera {
    private final Vector3f REUSABLE_RAY_VECTOR = new Vector3f().zero();
    private final Matrix4f viewMatrix = new Matrix4f();
    private boolean viewMatrixChanged = true;
    protected final Vector3f REUSABLE_UP_VECTOR = new Vector3f(0, 1, 0);
    protected final Vector3f REUSABLE_3D_VECTOR = new Vector3f().zero();
    protected final Vector3f position = new Vector3f(0, 0, 0);
    // Yaw is initialized to -90.0 degrees since a yaw of 0.0 results in a direction vector pointing to the right
    // (due to how Eular angles work) so we initially rotate a bit to the left.
    protected final Vector3f rotation = new Vector3f(0, -90, 0);
    protected final Matrix4f projectionMatrix = new Matrix4f();
    protected final Vector3f camFrontVector = new Vector3f(0, 0, -1);
    protected boolean projectionMatrixChanged = true;
    protected boolean positionChanged = true;
    protected Runnable projectionMatrixUpdater;

    public AbstractCamera() {
        MessageBus.register(LifeCycle.GAME_START.eventID(), this::onStart);
        MessageBus.register(LifeCycle.BEFORE_BUFFER_SWAP.eventID(), this::onBeforeBufferSwap);
        MessageBus.register(LifeCycle.WINDOW_RESIZED.eventID(), this::onWindowResized);
    }

    public void setPosition(Vector3f position) {
        if (this.position.equals(position)) {
            return;
        }

        this.position.set(position);
        updateViewMatrix();
        positionChanged = true;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation.set(rotation);
        recalculateFrontVector();
        updateViewMatrix();
    }

    public Rayf getRay() {
        return new Rayf(position, viewMatrix.positiveZ(REUSABLE_RAY_VECTOR).negate());
    }

    public void forceUpdate() {
        viewMatrixChanged = true;
        projectionMatrixChanged = true;
        positionChanged = true;
    }

    public void move(Vector3f moveVector) {
        if (moveVector.x != 0) {
            REUSABLE_3D_VECTOR.set(camFrontVector);
            position.add(REUSABLE_3D_VECTOR.cross(REUSABLE_UP_VECTOR).normalize().mul(moveVector.x));
            positionChanged = true;
        }

        if (moveVector.z != 0) {
            REUSABLE_3D_VECTOR.set(camFrontVector);
            position.add(REUSABLE_3D_VECTOR.mul(moveVector.z));
            positionChanged = true;
        }

        if (moveVector.y != 0) {
            position.y += moveVector.y;
            positionChanged = true;
        }

        if (positionChanged) {
            updateViewMatrix();
        }
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public boolean isViewMatrixChanged() {
        return viewMatrixChanged;
    }

    public boolean isProjectionMatrixChanged() {
        return projectionMatrixChanged;
    }

    public boolean isPositionChanged() {
        return positionChanged;
    }

    protected void updateViewMatrix() {
        viewMatrix.identity();
        REUSABLE_3D_VECTOR.set(position);

        viewMatrix.lookAt(position, REUSABLE_3D_VECTOR.add(camFrontVector), REUSABLE_UP_VECTOR);
        viewMatrixChanged = true;
    }

    private void onStart() {
        projectionMatrixUpdater.run();
        updateViewMatrix();
    }

    private void onBeforeBufferSwap() {
        if (viewMatrixChanged) {
            viewMatrixChanged = false;
        }

        if (projectionMatrixChanged) {
            projectionMatrixChanged = false;
        }

        if (positionChanged) {
            positionChanged = false;
        }
    }

    private void onWindowResized() {
        projectionMatrixUpdater.run();
        updateViewMatrix();
    }

    private void recalculateFrontVector() {
        camFrontVector.x = (float) (Math.cos(Math.toRadians(rotation.x)) * Math.cos(Math.toRadians(rotation.y)));
        camFrontVector.y = (float) (Math.sin(Math.toRadians(rotation.x)));
        camFrontVector.z = (float) (Math.cos(Math.toRadians(rotation.x)) * Math.sin(Math.toRadians(rotation.y)));
        camFrontVector.normalize();
    }
}
