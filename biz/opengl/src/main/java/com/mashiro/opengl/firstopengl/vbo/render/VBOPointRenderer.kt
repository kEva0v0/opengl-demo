package com.mashiro.opengl.firstopengl.vbo.render

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import com.mashiro.opengl.firstopengl.normal.viewmodel.TriangleViewModel
import com.mashiro.opengl.firstopengl.vbo.program.TestVBOProgram
import com.mashiro.opengl.firstopengl.vbo.viewmodel.VBOViewModel
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VBOPointRenderer(private val context: Context, private val viewModel: VBOViewModel): GLSurfaceView.Renderer {

    private var vboProgram: TestVBOProgram? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        vboProgram = TestVBOProgram(context)
        vboProgram?.initVBO(viewModel.normalPoint)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GLES20.GL_COLOR_BUFFER_BIT)
        vboProgram?.drawFrame(viewModel.normalPoint)
    }
}