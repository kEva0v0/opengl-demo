package com.mashiro.opengl.firstopengl.normal.program

import android.content.Context
import android.opengl.GLES20
import com.mashiro.opengl.firstopengl.R
import com.mashiro.opengl.firstopengl.api.BaseProgram
import com.mashiro.opengl.firstopengl.utils.BufferUtil
import java.nio.FloatBuffer
import java.util.*

class TestProgram(context: Context): BaseProgram<String>(context) {

    companion object {
        private val POSITION_COMPONENT_COUNT = 2
        private val POINT_DATA = floatArrayOf(0f, 0f)
    }
    
    private val mVertexData: FloatBuffer
    
    private var uColorLocation: Int = 0
    private val mRandom = Random()
    private var aPositionLocation :Int = 0
    
    init {
        mVertexData = BufferUtil.createFloatBuffer(POINT_DATA)
    }
    
    override fun vertexProgramResId(): Int = R.raw.test_vertex_shader

    override fun fragmentProgramResId(): Int = R.raw.test_fragment_shader

    override fun attributeList(): List<String> = listOf()

    override fun initNativeHandle() {
        aPositionLocation = GLES20.glGetAttribLocation(programId, "a_Position")
        uColorLocation = GLES20.glGetUniformLocation(programId, "u_Color")
    }

    override fun bindAttributes(model: String) {
        mVertexData.position(0)
        GLES20.glVertexAttribPointer(
            aPositionLocation,
            POSITION_COMPONENT_COUNT, 
            GLES20.GL_FLOAT,
            false, 0, mVertexData)
        GLES20.glEnableVertexAttribArray(aPositionLocation)
    }

    override fun setUniforms(model: String) {
        GLES20.glUniform4f(uColorLocation, mRandom.nextFloat(), mRandom.nextFloat(), mRandom.nextFloat(), 1.0f)
    }

    override fun draw(model: String) {
        // 只要持有传递给GL层的Buffer引用，就可以动态改变相关的数据信息
        mVertexData.put(floatArrayOf(0.9f * mRandom.nextFloat() * (if (mRandom.nextFloat() > 0.5f) 1 else -1).toFloat(), 0.9f * mRandom.nextFloat() * (if (mRandom.nextFloat() > 0.5f) 1 else -1).toFloat()))
        mVertexData.position(0)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
    }

    override fun disableAttributes() {

    }

}