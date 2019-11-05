package spck.engine.render.shader;

public enum ShaderUniform {
    CAMERA_VIEW_MATRIX,
    CAMERA_INVERSE_VIEW_MATRIX,
    CAMERA_PROJECTION_MATRIX,
    CAMERA_POSITION;

    public enum Material {
        HAS_DIFFUSE_TEXTURE("MATERIAL.HAS_DIFFUSE_TEXTURE"),
        DIFFUSE_TEXTURE_SAMPLER("MATERIAL.DIFFUSE_TEXTURE_SAMPLER"),
        DIFFUSE_COLOR("MATERIAL.DIFFUSE_COLOR"),
        SHININESS("MATERIAL.SHININESS");

        private String uniformName;

        Material(String uniformName) {
            this.uniformName = uniformName;
        }

        public String getUniformName() {
            return uniformName;
        }
    }

    public enum ILLUMINATION {
        AMBIENT_LIGHT_COLOR("ILLUMINATION.ambientLight.color"),
        AMBIENT_LIGHT_STRENGTH("ILLUMINATION.ambientLight.strength"),
        LIGHTS_ARRAY("ILLUMINATION.lights"),
        LIGHT_TYPE(".type"),

        LIGHT_DIRECTIONAL_COLOR(".directionalLight.color"),
        LIGHT_DIRECTIONAL_STRENGTH(".directionalLight.strength"),
        LIGHT_DIRECTIONAL_DIRECTION(".directionalLight.direction"),

        LIGHT_POINT_COLOR(".pointLight.color"),
        LIGHT_POINT_STRENGTH(".pointLight.strength"),
        LIGHT_POINT_POSITION(".pointLight.position"),
        LIGHT_POINT_ATTENUATION_CONSTANT(".pointLight.attenuation.constant"),
        LIGHT_POINT_ATTENUATION_LINEAR(".pointLight.attenuation.linear"),
        LIGHT_POINT_ATTENUATION_QUADRATIC(".pointLight.attenuation.quadratic"),

        LIGHT_SPOT_COLOR(".spotLight.color"),
        LIGHT_SPOT_STRENGTH(".spotLight.strength"),
        LIGHT_SPOT_POSITION(".spotLight.position"),
        LIGHT_SPOT_CONE_DIRECTION(".spotLight.coneDirection"),
        LIGHT_SPOT_CUTOFF(".spotLight.cutOff"),
        LIGHT_SPOT_ATTENUATION_CONSTANT(".spotLight.attenuation.constant"),
        LIGHT_SPOT_ATTENUATION_LINEAR(".spotLight.attenuation.linear"),
        LIGHT_SPOT_ATTENUATION_QUADRATIC(".spotLight.attenuation.quadratic"),
        ;


        private String uniformName;

        ILLUMINATION(String uniformName) {
            this.uniformName = uniformName;
        }

        public String getUniformName() {
            return uniformName;
        }
    }
}
