package com.mashiro.opengl.firstopengl.normal.bean

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

data class Triangle(
    val pointList: List<Point>
){
    val vertexData by lazy {
        FloatArray(pointList.size * 7)
            .apply { 
                for (i in 0 until 3) {
                    this[i*7+0] = pointList[i].x
                    this[i*7+1] = pointList[i].y
                    this[i*7+2] = pointList[i].z
                    this[i*7+3] = pointList[i].r / 255f
                    this[i*7+4] = pointList[i].g / 255f
                    this[i*7+5] = pointList[i].b / 255f
                    this[i*7+6] = pointList[i].a / 255f
                }
            }
            .toFloatBuffer()
    }

    private fun FloatArray.toFloatBuffer(): FloatBuffer {
        return ByteBuffer.allocateDirect(this.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(this@toFloatBuffer)
            }
    }
}