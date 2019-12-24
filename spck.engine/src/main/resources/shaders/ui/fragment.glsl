#version 410 core

precision mediump float;

uniform sampler2D TEXTURE;

in vec2 UV_COORDS;
in vec4 FRAG_COLOR;

layout (location = 0) out vec4 FINAL_COLOR;

void main(){
   FINAL_COLOR = FRAG_COLOR * texture(TEXTURE, UV_COORDS.st);
}
