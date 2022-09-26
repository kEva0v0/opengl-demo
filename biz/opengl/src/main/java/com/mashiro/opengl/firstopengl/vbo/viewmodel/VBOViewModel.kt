package com.mashiro.opengl.firstopengl.vbo.viewmodel

import com.mashiro.opengl.firstopengl.BaseOpenGLViewModel
import com.mashiro.opengl.firstopengl.normal.bean.Point
import com.mashiro.opengl.firstopengl.vbo.bean.NormalPoint
import java.util.*

class VBOViewModel: BaseOpenGLViewModel() {

    val normalPoint = NormalPoint(mutableListOf(Point(1.0f,1.0f,0f), Point(0f, 1f, 0f), Point(0f, 0f, 0f)))
    private val mRandom = Random()

    override fun addPoint() {
        normalPoint.pointList.add(
            Point(
                0.9f * mRandom.nextFloat() * (if (mRandom.nextFloat() > 0.5f) 1 else -1).toFloat(),
                0.9f * mRandom.nextFloat() * (if (mRandom.nextFloat() > 0.5f) 1 else -1).toFloat(),
                0f
            )
        )
    }

    override fun removeLastPoint() {
        normalPoint.pointList.removeLastOrNull()
    }

    override fun clearAll() {
        normalPoint.pointList.clear()
    }
}