package spck.engine.render.shader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.render.Material;
import spck.engine.render.camera.Camera;

public class PolygonShader extends AbstractShader implements Shader {
    private final static Logger LOGGER = LoggerFactory.getLogger(PolygonShader.class);
    private final static String vertexShader = "/shaders/polygon/vertex.glsl";
    private final static String fragmenthader = "/shaders/polygon/fragment.glsl";
    private final Camera camera;
    private int vertexID;

    public PolygonShader(Camera camera) {
        super(PolygonShader.class);
        this.camera = camera;
    }

    @Override
    public void init() {
        super.init();
        LOGGER.debug("Initialising shader...");

        vertexID = attachVertexShader(vertexShader);
        attachFragmentShader(fragmenthader);

        createUniform(vertexID, ShaderUniform.CAMERA_PROJECTION_MATRIX);
        createUniform(vertexID, ShaderUniform.CAMERA_VIEW_MATRIX);
        setUniform(vertexID, ShaderUniform.CAMERA_VIEW_MATRIX, camera.getViewMatrix());
        setUniform(vertexID, ShaderUniform.CAMERA_PROJECTION_MATRIX, camera.getProjectionMatrix());
    }

    @Override
    public void startShader(Material noMaterialExpected) {
        bind();

        if (camera.isViewMatrixChanged()) {
            setUniform(vertexID, ShaderUniform.CAMERA_VIEW_MATRIX, camera.getViewMatrix());
        }

        if (camera.isProjectionMatrixChanged()) {
            setUniform(vertexID, ShaderUniform.CAMERA_PROJECTION_MATRIX, camera.getProjectionMatrix());
        }
    }

    @Override
    public void stopShader() {
        unbind();
    }
}
