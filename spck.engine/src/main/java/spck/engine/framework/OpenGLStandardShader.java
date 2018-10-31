package spck.engine.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spck.engine.lights.DirectionalLight;
import spck.engine.lights.Light;
import spck.engine.lights.LightSystem;
import spck.engine.lights.PointLight;
import spck.engine.lights.SpotLight;
import spck.engine.render.AbstractShader;
import spck.engine.render.Camera;
import spck.engine.render.Material;
import spck.engine.render.Shader;
import spck.engine.render.ShaderUniform;
import spck.engine.util.KeyValueTool;

public class OpenGLStandardShader extends AbstractShader implements Shader {
    private final static Logger LOGGER = LoggerFactory.getLogger(OpenGLStandardShader.class);
    private final static String vertexShader = "/shaders/standard/vertex.glsl";
    private final static String fragmenthader = "/shaders/standard/fragment.glsl";
    private final Camera camera;
    private int vertexID;
    private int fragmentID;

    public OpenGLStandardShader(Camera camera) {
        super(OpenGLStandardShader.class);
        this.camera = camera;
    }

    @Override
    public void init() {
        super.init();
        LOGGER.debug("Initialising shader...");

        // Camera has to be updated at least one, when we run startShader
        camera.forceUpdate();

        vertexID = attachVertexShader(vertexShader);
        fragmentID = attachFragmentShader(fragmenthader);

        createUniform(vertexID, ShaderUniform.CAMERA_PROJECTION_MATRIX);
        createUniform(vertexID, ShaderUniform.CAMERA_VIEW_MATRIX);
        createUniform(fragmentID, ShaderUniform.CAMERA_POSITION);

        // material
        createUniform(fragmentID, ShaderUniform.Material.HAS_DIFFUSE_TEXTURE.getUniformName());
        createUniform(fragmentID, ShaderUniform.Material.DIFFUSE_TEXTURE_SAMPLER.getUniformName());
        createUniform(fragmentID, ShaderUniform.Material.DIFFUSE_COLOR.getUniformName());
        createUniform(fragmentID, ShaderUniform.Material.SHININESS.getUniformName());

        // lights
        createUniform(fragmentID, ShaderUniform.ILLUMINATION.AMBIENT_LIGHT_COLOR.getUniformName());
        createUniform(fragmentID, ShaderUniform.ILLUMINATION.AMBIENT_LIGHT_STRENGTH.getUniformName());

        for (int i = 0; i < LightSystem.MAX_NUM_LIGHTS; i++) {
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_TYPE.getUniformName());
            // directional
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_DIRECTIONAL_COLOR.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_DIRECTIONAL_STRENGTH.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_DIRECTIONAL_DIRECTION.getUniformName());

            // point
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_POINT_COLOR.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_POINT_STRENGTH.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_POINT_POSITION.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_POINT_ATTENUATION_CONSTANT.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_POINT_ATTENUATION_LINEAR.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_POINT_ATTENUATION_QUADRATIC.getUniformName());

            // spot
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_COLOR.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_STRENGTH.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_POSITION.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_CONE_DIRECTION.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_CUTOFF.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_ATTENUATION_CONSTANT.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_ATTENUATION_LINEAR.getUniformName());
            createUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + i + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_ATTENUATION_QUADRATIC.getUniformName());
        }
    }

    @Override
    public void startShader(Material material) {
        bind();

        updateCamera();
        updateMaterial(material);
        updateLights();
    }

    private void updateLights() {
        LightSystem.getAmbientLight().ifPresent(ambientLight -> {
            if (ambientLight.isChanged()) {
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.AMBIENT_LIGHT_COLOR.getUniformName(), ambientLight.getColor());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.AMBIENT_LIGHT_STRENGTH.getUniformName(), ambientLight.getStrength());
            }
        });

        int index = 0;
        for (Light light : LightSystem.getLights()) {
            if (!light.isChanged()) {
                continue;
            }

            if (light instanceof PointLight) {
                PointLight pointLight = (PointLight) light;
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_TYPE.getUniformName(), Light.Type.POINT.getShaderCode());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_POINT_COLOR.getUniformName(), pointLight.getColor());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_POINT_STRENGTH.getUniformName(), pointLight.getStrength());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_POINT_POSITION.getUniformName(), pointLight.getPosition());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_POINT_ATTENUATION_CONSTANT.getUniformName(), pointLight.getAttenuation().getConstant());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_POINT_ATTENUATION_LINEAR.getUniformName(), pointLight.getAttenuation().getLinear());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_POINT_ATTENUATION_QUADRATIC.getUniformName(), pointLight.getAttenuation().getQuadratic());
            } else if (light instanceof DirectionalLight) {
                DirectionalLight directionalLight = (DirectionalLight) light;
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_TYPE.getUniformName(), Light.Type.DIRECTIONAL.getShaderCode());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_DIRECTIONAL_COLOR.getUniformName(), directionalLight.getColor());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_DIRECTIONAL_STRENGTH.getUniformName(), directionalLight.getStrength());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_DIRECTIONAL_DIRECTION.getUniformName(), directionalLight.getDirection());
            } else if (light instanceof SpotLight) {
                SpotLight spotLight = (SpotLight) light;
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_TYPE.getUniformName(), Light.Type.SPOT.getShaderCode());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_COLOR.getUniformName(), spotLight.getColor());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_STRENGTH.getUniformName(), spotLight.getStrength());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_POSITION.getUniformName(), spotLight.getPosition());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_CONE_DIRECTION.getUniformName(), spotLight.getConeDirection());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_CUTOFF.getUniformName(), spotLight.getCutOff());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_ATTENUATION_CONSTANT.getUniformName(), spotLight.getAttenuation().getConstant());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_ATTENUATION_LINEAR.getUniformName(), spotLight.getAttenuation().getLinear());
                setUniform(fragmentID, ShaderUniform.ILLUMINATION.LIGHTS_ARRAY.getUniformName() + "[" + index + "]" + ShaderUniform.ILLUMINATION.LIGHT_SPOT_ATTENUATION_QUADRATIC.getUniformName(), spotLight.getAttenuation().getQuadratic());
            }

            light.ackChange();
            index++;
        }
    }

    private void updateMaterial(Material material) {
        setUniform(fragmentID, ShaderUniform.Material.HAS_DIFFUSE_TEXTURE.getUniformName(), material.hasDiffuseTexture());
        setUniform(fragmentID, ShaderUniform.Material.DIFFUSE_COLOR.getUniformName(), material.getDiffuseColor());
        setUniform(fragmentID, ShaderUniform.Material.SHININESS.getUniformName(), material.getShininess());

        if (material.hasDiffuseTexture()) {
            String key = fragmentID + material.getDiffuseTexture().getShaderSamplerName();
            int samplerIndex = material.getDiffuseTexture().getSamplerIndex();

            KeyValueTool.computeIfValueChanged(
                    key,
                    samplerIndex,
                    () -> setUniform(fragmentID, material.getDiffuseTexture().getShaderSamplerName(), samplerIndex)
            );
        }
    }

    private void updateCamera() {
        if (camera.isPositionChanged()) {
            setUniform(fragmentID, ShaderUniform.CAMERA_POSITION, camera.getPosition());
        }

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
