#version 410 core

layout (location = 3) in mat4 transformationMatrixInstanced;
layout (location = 9) in vec3 aabbVxPosition;

uniform mat4 CAMERA_PROJECTION_MATRIX;
uniform mat4 CAMERA_VIEW_MATRIX;

out gl_PerVertex {
    vec4 gl_Position;
};

void main(){
    vec4 vertexWorldPosition=transformationMatrixInstanced*vec4(aabbVxPosition, 1.0);
    gl_Position = CAMERA_PROJECTION_MATRIX * CAMERA_VIEW_MATRIX * vertexWorldPosition;
}