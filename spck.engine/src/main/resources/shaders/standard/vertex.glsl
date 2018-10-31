#version 410 core

layout (location = 0) in vec3 ATTR_VX_POSITION;
layout (location = 1) in vec3 ATTR_VX_NORMAL;
layout (location = 2) in vec2 ATTR_VX_UV_COORDS;
layout (location = 3) in mat4 ATTR_INS_TRANSFORMATION_MATRIX;
layout (location = 7) in float ATTR_INS_UV_SCALE;
layout (location = 8) in vec2 ATTR_INS_UV_OFFSET;

uniform mat4 CAMERA_PROJECTION_MATRIX;
uniform mat4 CAMERA_VIEW_MATRIX;

out VERTEX_SHADER_OUT {
    vec3 vxPosition;
    vec3 vxNormal;
    vec2 vxUVCoords;
} SHADER_OUTPUT;

out gl_PerVertex {
    vec4 gl_Position;
};

void main() {
    vec4 vertexWorldPosition=ATTR_INS_TRANSFORMATION_MATRIX * vec4(ATTR_VX_POSITION,1.0);

    SHADER_OUTPUT.vxPosition = vertexWorldPosition.xyz;
    SHADER_OUTPUT.vxNormal = normalize(ATTR_VX_NORMAL);
    SHADER_OUTPUT.vxUVCoords = ATTR_VX_UV_COORDS / ATTR_INS_UV_SCALE + ATTR_INS_UV_OFFSET;

    gl_Position = CAMERA_PROJECTION_MATRIX * CAMERA_VIEW_MATRIX * vertexWorldPosition;
}