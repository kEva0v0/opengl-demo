package com.mashiro.opengl.firstopengl.normal.render

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import com.mashiro.opengl.firstopengl.normal.viewmodel.TriangleViewModel
import com.mashiro.opengl.firstopengl.normal.program.TestProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PointRenderer(private val context: Context, private val viewModel: TriangleViewModel): GLSurfaceView.Renderer {

    private var triangleProgram: TestProgram? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        triangleProgram = TestProgram(context)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GLES20.GL_COLOR_BUFFER_BIT)
        viewModel.getList().forEach {
            triangleProgram?.drawFrame("")
        }
    }

}