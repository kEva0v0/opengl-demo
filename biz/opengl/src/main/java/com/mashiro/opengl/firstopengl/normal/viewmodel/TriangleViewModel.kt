package com.mashiro.opengl.firstopengl.normal.viewmodel

import androidx.lifecycle.ViewModel
import com.mashiro.opengl.firstopengl.BaseOpenGLViewModel
import com.mashiro.opengl.firstopengl.normal.bean.Point
import com.mashiro.opengl.firstopengl.normal.bean.Triangle

class TriangleViewModel: BaseOpenGLViewModel(){

    private var triangleList = mutableListOf<Triangle>()

    private var startTriangle = Triangle(
        listOf(
            Point(100f, 100f, 0f, 255, 255, 255, 1),
            Point(100f, 300f, 0f, 255, 255, 255, 1),
            Point(100f, 500f, 0f, 255, 255, 255, 1)
        )
    )

    fun getList(): List<Triangle>{
        return triangleList
    }

    override fun addPoint(){
        val pointList = mutableListOf<Point>().apply {
            startTriangle.pointList.forEach {
                this.add(it.copy(x = it.x + 100))
            }
        }
        triangleList.add(Triangle(pointList))
    }

    override fun removeLastPoint() {
        if (triangleList.isNotEmpty()) {
            triangleList.removeLastOrNull()
        }
    }

    override fun clearAll(){
        triangleList.clear()
    }
}