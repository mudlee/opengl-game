#version 410 core

#define MAX_NUM_LIGHTS 10
#define LIGHT_TYPE_DIRECTIONAL 0
#define LIGHT_TYPE_POINT 1
#define LIGHT_TYPE_SPOT 2

layout (location = 0) out vec4 FINAL_COLOR;

struct Material {
    bool HAS_DIFFUSE_TEXTURE;
    sampler2D DIFFUSE_TEXTURE_SAMPLER;
    vec3 DIFFUSE_COLOR;
    float SHININESS;
};

struct Attenuation{
    float constant;
    float linear;
    float quadratic;
};

struct AmbientLight {
    vec4 color;
    float strength;
};

struct DirectionalLight {
    vec4 color;
    float strength;
    vec3 direction;
};

struct PointLight {
    vec4 color;
    float strength;
    vec3 position;
    Attenuation attenuation;
};

struct SpotLight{
    vec4 color;
    float strength;
    vec3 position;
    vec3 coneDirection;
    float cutOff;
    Attenuation attenuation;
};

struct Light {
    int type;
    PointLight pointLight;
    DirectionalLight directionalLight;
    SpotLight spotLight;
};

struct Illumination {
    AmbientLight ambientLight;
    Light lights[MAX_NUM_LIGHTS];
};

uniform Material MATERIAL;
uniform Illumination ILLUMINATION;
uniform vec3 CAMERA_POSITION;

in VERTEX_SHADER_OUT {
    vec3 vxPosition;
    vec3 vxNormal;
    vec2 vxUVCoords;
} SHADER_INPUT;

vec4 calculateLightColor(vec4 color, float strength, vec3 toLightDirection, vec3 vxPosition, vec3 vxNormal, float materialShininess){
    const float PI = 3.14159265;

    vec4 diffuseColor=vec4(0.0);
    vec4 specularColor=vec4(0.0);

    // diffuse calculation
    float diffuseFactor=max(dot(vxNormal,toLightDirection),0.0);
    diffuseColor=color*strength*diffuseFactor;

    // specular calculation
    // Blinn-Phong
    vec3 viewDirection=normalize(CAMERA_POSITION - vxPosition);
    vec3 halfwayDir = normalize(toLightDirection + viewDirection);
    float energyConservation = ( 8.0 + materialShininess ) / ( 8.0 * PI );
    float specularFactor = energyConservation * pow(max(dot(vxNormal, halfwayDir), 0.0), materialShininess);
    specularColor=color*strength*specularFactor;

    if(diffuseColor==vec4(0.0)){
        specularColor=vec4(0.0);
    }

    return diffuseColor + specularColor;
}

vec4 calculateAmbientLight(){
    return vec4(
        vec3(ILLUMINATION.ambientLight.color.rgb) * ILLUMINATION.ambientLight.strength,
        ILLUMINATION.ambientLight.color.a
    );
}

vec4 calculateDirectionalLight(DirectionalLight light, vec3 vxPosition, vec3 vxNormal, float materialShininess){
    return calculateLightColor(light.color, light.strength, normalize(light.direction), vxPosition, vxNormal, materialShininess);
}

vec4 calculatePointLight(PointLight pointLight, vec3 vxPosition, vec3 vxNormal, float materialShininess){
    vec3 distanceFromLightVector = pointLight.position - vxPosition;
    vec3 toLightDirection = normalize(distanceFromLightVector);
    vec4 color = calculateLightColor(pointLight.color, pointLight.strength, toLightDirection, vxPosition, vxNormal, materialShininess);

    // attenuation
    float distance = length(distanceFromLightVector);
    float attenuationFactor = pointLight.attenuation.constant + (distance*pointLight.attenuation.linear) + (distance*distance*pointLight.attenuation.quadratic);

    return color / attenuationFactor;
}

vec4 calculateSpotLight(SpotLight spotLight, vec3 vxPosition, vec3 vxNormal,float materialShininess){
    vec3 distanceFromLightVector = spotLight.position - vxPosition;
    vec3 toLightDirection = normalize(distanceFromLightVector);
    vec3 fromLightDirection = -toLightDirection;

    // how close are we to being in the spot
    float spotCos = dot(fromLightDirection, normalize(spotLight.coneDirection));

    if(spotCos>spotLight.cutOff){
        vec4 color = calculateLightColor(spotLight.color, spotLight.strength, toLightDirection, vxPosition, vxNormal, materialShininess);

        float distance = length(distanceFromLightVector);
        float attenuationFactor = spotLight.attenuation.constant + (distance*spotLight.attenuation.linear) + (distance*distance*spotLight.attenuation.quadratic);
        color = color / attenuationFactor;
        color *= (1.0 - (1.0 - spotCos) / (1.0 - spotLight.cutOff));
        return color;
    }

    return vec4(0.0);
}

vec4 calculateIllumination(vec4 currentColor, float materialShininess){
    vec4 totalLight=vec4(0.0);

    for(int i=0;i<MAX_NUM_LIGHTS;i++){
        Light light = ILLUMINATION.lights[i];
        if(light.type == LIGHT_TYPE_DIRECTIONAL){
            totalLight += calculateDirectionalLight(light.directionalLight, SHADER_INPUT.vxPosition, SHADER_INPUT.vxNormal, materialShininess);
        }
        else if(light.type == LIGHT_TYPE_POINT){
            totalLight += calculatePointLight(light.pointLight, SHADER_INPUT.vxPosition, SHADER_INPUT.vxNormal, materialShininess);
        }
        else if(light.type == LIGHT_TYPE_SPOT){
            totalLight += calculateSpotLight(light.spotLight, SHADER_INPUT.vxPosition, SHADER_INPUT.vxNormal, materialShininess);
        }
    }

    return totalLight;
}

void main() {
    if(MATERIAL.HAS_DIFFUSE_TEXTURE){
        FINAL_COLOR = texture(MATERIAL.DIFFUSE_TEXTURE_SAMPLER, SHADER_INPUT.vxUVCoords);
    }
    else{
        FINAL_COLOR = vec4(MATERIAL.DIFFUSE_COLOR,1.0f);
    }

    FINAL_COLOR = FINAL_COLOR * calculateAmbientLight() + calculateIllumination(FINAL_COLOR, MATERIAL.SHININESS);
}