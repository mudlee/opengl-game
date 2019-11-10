#version 410 core

layout (location = 0) in vec3 aabbVxPosition;

uniform mat4 CAMERA_PROJECTION_MATRIX;
uniform mat4 CAMERA_VIEW_MATRIX;

out gl_PerVertex {
    vec4 gl_Position;
};

void main(){
    vec4 vertexWorldPosition = vec4(aabbVxPosition, 1.0);
    gl_Position = CAMERA_PROJECTION_MATRIX * CAMERA_VIEW_MATRIX * vertexWorldPosition;
}