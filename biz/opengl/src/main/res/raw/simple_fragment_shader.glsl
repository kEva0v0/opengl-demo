precision mediump float;

uniform float u_Alpha;

varying vec4 v_Color;

void main() {
    vec4 color = v_Color;
    color.a = u_Alpha;
    gl_FragColor = color;
}