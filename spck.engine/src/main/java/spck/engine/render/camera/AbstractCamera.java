package spck.engine.render.camera;

import org.joml.Math;
import org.joml.*;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.window.GLFWWindow;

public class AbstractCamera {
    private final Vector3f REUSABLE_RAY_VECTOR = new Vector3f().zero();
    private final Vector4f WORLD_POS_TEMP = new Vector4f();
    private final Vector4f CLIP_SPACE_TEMP = new Vector4f();
    private final Vector3f NDC_SPACE_TEMP = new Vector3f();
    private final Vector2f WINDOW_SPACE_TEMP = new Vector2f();
    private final Vector2f WINDOW_SIZE_TEMP = new Vector2f();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final GLFWWindow window;
    protected boolean viewMatrixChanged = true;
    protected final Vector3f REUSABLE_UP_VECTOR = new Vector3f(0, 1, 0);
    protected final Vector3f REUSABLE_3D_VECTOR = new Vector3f().zero();
    protected final Vector3f position = new Vector3f(0, 0, 0);
    protected final Vector3f rotation = new Vector3f(0, 0, 0);
    protected final Matrix4f projectionMatrix = new Matrix4f();
    protected final Vector3f camFrontVector = new Vector3f(0, 0, -1); // RIGHT HANDED
    protected boolean projectionMatrixChanged = true;
    protected boolean positionChanged = true;
    protected Runnable projectionMatrixUpdater;

    public AbstractCamera(GLFWWindow window) {
        this.window = window;
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

    public Vector2f worldSpaceToScreenSpace(Vector3f worldPosition) {
        WORLD_POS_TEMP.set(worldPosition, 1f);
        WINDOW_SIZE_TEMP.set(window.getWindowWidth(), window.getWindowHeight());

        CLIP_SPACE_TEMP.set(WORLD_POS_TEMP).mul(viewMatrix).mul(projectionMatrix);
        NDC_SPACE_TEMP.set(CLIP_SPACE_TEMP.x, -CLIP_SPACE_TEMP.y, CLIP_SPACE_TEMP.z).div(CLIP_SPACE_TEMP.w);
        WINDOW_SPACE_TEMP.set(NDC_SPACE_TEMP.x + 1f, NDC_SPACE_TEMP.y + 1f);
        WINDOW_SPACE_TEMP.set(WINDOW_SPACE_TEMP.x / 2f, WINDOW_SPACE_TEMP.y / 2f);
        WINDOW_SPACE_TEMP.mul(WINDOW_SIZE_TEMP);
        return WINDOW_SPACE_TEMP;
    }

    public Vector3f getFrontVector() {
        return camFrontVector;
    }

    public Rayf getRay() {
        return new Rayf(position, viewMatrix.positiveZ(REUSABLE_RAY_VECTOR).negate());
    }

    public void forceUpdate() {
        viewMatrixChanged = true;
        projectionMatrixChanged = true;
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
        camFrontVector.x = (float) (Math.sin(Math.toRadians(rotation.y)) * Math.cos(Math.toRadians(rotation.x)));
        camFrontVector.y = (float) (Math.sin(Math.toRadians(rotation.x)));
        camFrontVector.z = (float) (-Math.cos(Math.toRadians(rotation.y)) * Math.cos(Math.toRadians(rotation.x)));
        camFrontVector.normalize();
    }
}
