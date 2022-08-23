uniform mat4 u_Mvp;
attribute vec4 a_Position;

void main(){
    gl_Position = u_Mvp * vec4(a_Position.x, a_Position.y, a_Position.z, a_Position.w);
    gl_PointSize = 40.0;
}