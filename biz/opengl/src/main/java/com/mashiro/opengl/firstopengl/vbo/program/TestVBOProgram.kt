package com.mashiro.opengl.firstopengl.vbo.program

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES31
import android.opengl.Matrix
import android.util.Log
import com.mashiro.opengl.firstopengl.R
import com.mashiro.opengl.firstopengl.api.BaseProgram
import com.mashiro.opengl.firstopengl.vbo.bean.NormalPoint
import java.util.*

class TestVBOProgram(context: Context): BaseProgram<NormalPoint>(context) {

    private var uMvpHandle: Int = 0
    private var uColorLocation: Int = 0
    private val mRandom = Random()
    private var aPositionLocation :Int = 0

    private var vboIntArray = IntArray(1)

    fun initVBO(model: NormalPoint) {
        Log.i("zyc", "hello~")
        GLES20.glGenBuffers(1, vboIntArray, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboIntArray[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, NormalPoint.MAX_SIZE, null, GLES20.GL_STATIC_DRAW)
        model.vertexData.position(0)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,0, model.getBufferSize(), model.vertexData)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    override fun vertexProgramResId(): Int = R.raw.vbo_vertex_shader

    override fun fragmentProgramResId(): Int = R.raw.test_fragment_shader

    override fun attributeList(): List<String> = listOf()

    override fun initNativeHandle() {
        uMvpHandle = GLES31.glGetUniformLocation(programId, "u_Mvp")
        aPositionLocation = GLES20.glGetAttribLocation(programId, "a_Position")
        uColorLocation = GLES20.glGetUniformLocation(programId, "u_Color")
    }

    override fun bindAttributes(model: NormalPoint) {
//        model.vertexData.position(0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboIntArray[0])
        GLES20.glVertexAttribPointer(
            aPositionLocation,
            NormalPoint.POSITION_COMPONENT_COUNT,
            GLES20.GL_FLOAT,
            false,
            0,
            0
        )
        GLES20.glEnableVertexAttribArray(aPositionLocation)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    override fun setUniforms(model: NormalPoint) {
        GLES31.glUniformMatrix4fv(uMvpHandle, 1, false, model.vertexMatrix, 0)
        GLES20.glUniform4f(uColorLocation, mRandom.nextFloat(), mRandom.nextFloat(), mRandom.nextFloat(), 1.0f)
    }

    override fun draw(model: NormalPoint) {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, model.pointList.size)
//        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, model.pointList.size)
    }

    override fun disableAttributes() {

    }

}