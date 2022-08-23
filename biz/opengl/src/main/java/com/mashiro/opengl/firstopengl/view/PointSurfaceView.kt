package com.mashiro.opengl.firstopengl.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.mashiro.opengl.firstopengl.BaseOpenGLViewModel
import com.mashiro.opengl.firstopengl.normal.viewmodel.TriangleViewModel
import com.mashiro.opengl.firstopengl.normal.render.PointRenderer
import com.mashiro.opengl.firstopengl.vbo.render.VBOPointRenderer
import com.mashiro.opengl.firstopengl.vbo.viewmodel.VBOViewModel

class PointSurfaceView: GLSurfaceView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    init {
        setEGLContextClientVersion(3)
    }

    private lateinit var baseOpenGLViewModel: BaseOpenGLViewModel

    fun init(baseOpenGLViewModel: BaseOpenGLViewModel) {
        this.baseOpenGLViewModel = baseOpenGLViewModel
        if (baseOpenGLViewModel is TriangleViewModel){
            setRenderer(PointRenderer(context, baseOpenGLViewModel))
        }
        if (baseOpenGLViewModel is VBOViewModel) {
            setRenderer(VBOPointRenderer(context, baseOpenGLViewModel))
        }
        renderMode = RENDERMODE_WHEN_DIRTY
    }

}