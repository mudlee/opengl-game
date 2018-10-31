package spck.engine.render;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Rayf;
import org.joml.Vector3f;
import spck.engine.Engine;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;

public class Camera {
    private final Vector3f REUSABLE_UP_VECTOR = new Vector3f(0, 1, 0);
    private final Vector3f REUSABLE_RAY_VECTOR = new Vector3f().zero();
    private final Vector3f REUSABLE_3D_VECTOR = new Vector3f().zero();
    private final Vector3f position = new Vector3f(0, 0, 0);
    private final Vector3f rotation = new Vector3f(0, 0, 0);
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Vector3f camFrontVector = new Vector3f(1, 0, 0);
    private boolean viewMatrixChanged = true;
    private boolean projectionMatrixChanged = true;
    private boolean positionChanged = true;
    private Runnable projectionMatrixUpdater;

    private Camera() {
        MessageBus.register(LifeCycle.GAME_START.eventID(), this::onStart);
        MessageBus.register(LifeCycle.BEFORE_BUFFER_SWAP.eventID(), this::onBeforeBufferSwap);
        MessageBus.register(LifeCycle.WINDOW_RESIZED.eventID(), this::onWindowResized);
    }

    /**
     * Initialising a perspective projection.
     * <p>
     * Example: Camera.perspective(60f, 0.01f, 1000f);
     *
     * @param fov,   for default, use 60
     * @param zNear, for default, use 0.01
     * @param zFar,  for default, use 1000
     * @return Camera
     */
    public static Camera perspective(float fov, float zNear, float zFar) {
        Camera camera = new Camera();
        camera.projectionMatrixUpdater = () -> {
            float aspect = getWindowAspect();
            camera.projectionMatrix.setPerspective(
                    (float) java.lang.Math.toRadians(fov),
                    aspect,
                    zNear,
                    zFar
            );
            camera.projectionMatrixChanged = true;
        };
        return camera;
    }

    /**
     * Initialising an ortho projection.
     *
     * @param size,  for default, use 10
     * @param zNear, for default, use 0
     * @param zFar,  for default, use 1000
     * @return Camera
     */
    public static Camera ortho(int size, int zNear, int zFar) {
        Camera camera = new Camera();
        camera.projectionMatrixUpdater = () -> {
            float aspect = getWindowAspect();
            camera.projectionMatrix.setOrtho(-size * aspect, size * aspect, -size, size, zNear, zFar);
            camera.projectionMatrixChanged = true;
        };
        return camera;
    }

    private static float getWindowAspect() {
        return (float) Engine.window.getPreferences().getWidth() / (float) Engine.window.getPreferences().getHeight();
    }

    public void setRotation(Vector3f rotation) {
        this.rotation.set(rotation);
        recalculateFrontVector();
        updateViewMatrix();
    }

    public void forceUpdate() {
        viewMatrixChanged = true;
        projectionMatrixChanged = true;
        positionChanged = true;
    }

    public Rayf getRay() {
        return new Rayf(position, viewMatrix.positiveZ(REUSABLE_RAY_VECTOR).negate());
    }

    public void move(Vector3f movementVector) {
        if (movementVector.x != 0) {
            REUSABLE_3D_VECTOR.set(camFrontVector);
            position.add(REUSABLE_3D_VECTOR.cross(REUSABLE_UP_VECTOR).normalize().mul(movementVector.x));
            positionChanged = true;
        }

        if (movementVector.z != 0) {
            REUSABLE_3D_VECTOR.set(camFrontVector);
            position.add(REUSABLE_3D_VECTOR.mul(movementVector.z));
            positionChanged = true;
        }

        if (movementVector.y != 0) {
            position.y += movementVector.y;
            positionChanged = true;
        }

        if (positionChanged) {
            updateViewMatrix();
        }
    }

    public void setPosition(Vector3f position) {
        if (this.position.equals(position)) {
            return;
        }

        this.position.set(position);
        updateViewMatrix();
        positionChanged = true;
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

    private void updateViewMatrix() {
        REUSABLE_3D_VECTOR.set(position);

        viewMatrix.identity();
        viewMatrix.rotate((float) Math.toRadians(rotation.x), camFrontVector).rotate((float) Math.toRadians(rotation.y), REUSABLE_UP_VECTOR);
        viewMatrix.translate(-position.x, -position.y, -position.z);
        viewMatrixChanged = true;
    }
}
