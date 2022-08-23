package com.mashiro.opengl.firstopengl.vbo.bean

import com.mashiro.opengl.firstopengl.normal.bean.Point
import com.mashiro.opengl.firstopengl.utils.BufferUtil

data class NormalPoint(
    val pointList: MutableList<Point>,
    val vertexMatrix: FloatArray = floatArrayOf(1f,0f,0f,0f,0f,1f,0f,0f,0f,0f,1f,0f,0f,0f,0f,1f)
){
    companion object {
        const val POSITION_COLLECT_COUNT = 2
        const val POSITION_COMPONENT_COUNT = 4
        const val MAX_SIZE = 150000
    }

    val vertexData by lazy {
        val floatArray = FloatArray(pointList.size * POSITION_COMPONENT_COUNT)
        pointList.forEachIndexed { index, point ->
            floatArray[index*POSITION_COMPONENT_COUNT] = point.x
            floatArray[index*POSITION_COMPONENT_COUNT+1] = point.y
            floatArray[index*POSITION_COMPONENT_COUNT+2] = point.z
            floatArray[index*POSITION_COMPONENT_COUNT+3] = 1.0f
        }
        BufferUtil.createFloatBuffer(floatArray)
    }


    fun getBufferSize(): Int {
        return pointList.size * 4 * 4
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NormalPoint

        if (pointList != other.pointList) return false
        if (!vertexMatrix.contentEquals(other.vertexMatrix)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pointList.hashCode()
        result = 31 * result + vertexMatrix.contentHashCode()
        return result
    }

    fun moveLeft(){
        vertexMatrix[12] -= 0.1f
    }

    fun moveRight(){
        vertexMatrix[12] += 0.1f
    }

    fun moveUp() {
        vertexMatrix[13] += 0.1f
    }

    fun moveDown() {
        vertexMatrix[13] -= 0.1f
    }
}