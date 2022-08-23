package com.mashiro.opengl.firstopengl.api

import android.content.Context
import android.opengl.GLES31
import androidx.annotation.RawRes
import com.mashiro.opengl.firstopengl.utils.ShaderHelper
import java.io.IOException

abstract class BaseProgram<Model>(context: Context) {
    companion object {
        const val BYTE_PER_FLOAT = 4
        const val POSITION_COMPONENT_SIZE = 3
        const val NORMAL_COMPONENT_SIZE = 3
        const val COLOR_COMPONENT_SIZE = 4
    }

    protected var programId = 0

    init {
        compileProgram(context)
    }

    protected abstract fun vertexProgramResId(): Int

    protected abstract fun fragmentProgramResId(): Int

    protected abstract fun attributeList(): List<String>

    protected abstract fun initNativeHandle()

    protected abstract fun bindAttributes(model: Model)

    protected abstract fun setUniforms(model: Model)

    protected abstract fun draw(model: Model)

    protected abstract fun disableAttributes()

    open fun drawFrame(model: Model) {
        GLES31.glUseProgram(programId)
        initNativeHandle()
        bindAttributes(model)
        setUniforms(model)
        draw(model)
        disableAttributes()
    }

    private fun compileProgram(context: Context) {
        val vertexProgramText = readTextFileFromRawRes(context, vertexProgramResId())
        val fragmentProgramText = readTextFileFromRawRes(context, fragmentProgramResId())
        // 步骤1：编译顶点着色器
        val vertexShaderId = ShaderHelper.compileVertexShader(vertexProgramText)
        // 步骤2：编译片段着色器
        val fragmentShaderId = ShaderHelper.compileFragmentShader(fragmentProgramText)
        // 步骤3：将顶点着色器、片段着色器进行链接，组装成一个OpenGL程序
        programId = ShaderHelper.linkProgram(vertexShaderId, fragmentShaderId)
    }

    private fun readTextFileFromRawRes(context: Context, @RawRes resourceId: Int): String {
        val inputStream = context.resources.openRawResource(resourceId)
        try {
            val bytes = ByteArray(inputStream.available())
            inputStream.read(bytes)
            return String(bytes)
        } catch (e: IOException) {

        } finally {
            try {
                inputStream.close()
            } catch (ignored: Exception) {
            }
        }
        throw RuntimeException("Failed to read raw resource id $resourceId")
    }

    fun validateProgram(program: Int): Boolean {
        GLES31.glValidateProgram(program)
        val validateStatus = IntArray(1)
        GLES31.glGetProgramiv(program, GLES31.GL_VALIDATE_STATUS, validateStatus, 0)
        return validateStatus[0] != 0
    }

    /**
     * 编译着色器
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES31.glCreateShader(type)
        if (shader == 0) {
            return 0
        }

        GLES31.glShaderSource(shader, shaderCode)
        GLES31.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, compileStatus, 0)

        if (compileStatus[0] == 0) {
            GLES31.glDeleteShader(shader)
            return 0
        }
        return shader
    }
}