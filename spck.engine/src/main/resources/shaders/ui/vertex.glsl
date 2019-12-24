#version 410 core

layout (location = 0) in vec2 vxPosition;
layout (location = 1) in vec2 textureCoords;
layout (location = 2) in vec4 color;

uniform mat4 PROJECTION_MATRIX;

out vec2 UV_COORDS;
out vec4 FRAG_COLOR;

out gl_PerVertex {
   vec4 gl_Position;
};

void main() {
   UV_COORDS = textureCoords;
   FRAG_COLOR = color;
   gl_Position = PROJECTION_MATRIX * vec4(vxPosition.xy, 0, 1);
}