package com.mashiro.filament.bean

import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder


data class Vertex(val x: Float, val y: Float, val z: Float, val color: Int)

data class NormalPoint(
    val pointList: MutableList<Vertex>,
    val vertexMatrix: FloatArray = floatArrayOf(1f,0f,0f,0f,0f,1f,0f,0f,0f,0f,1f,0f,0f,0f,0f,1f)
){
    companion object {
        const val POSITION_COLLECT_COUNT = 2
        const val POSITION_COMPONENT_COUNT = 4
        const val MAX_SIZE = 150000

        const val intSize = 4
        const val floatSize = 4
        const val shortSize = 2
    }

    val vertexData: ByteBuffer by lazy {
        val byteBuffer = ByteBuffer.allocate(getPointSize()*getStripSize()).order(ByteOrder.nativeOrder())
        pointList.forEachIndexed { index, point ->
            byteBuffer.putFloat(point.x)
            byteBuffer.putFloat(point.y)
            byteBuffer.putFloat(point.z)
            // color
            byteBuffer.putFloat(1f)
            byteBuffer.putFloat(1f)
            byteBuffer.putFloat(0f)
            byteBuffer.putFloat(0f)
//            byteBuffer.putInt(point.color)
        }
        byteBuffer.flip()
        byteBuffer
    }

    val indexData: ByteBuffer by lazy {
        val indexData = ByteBuffer.allocate(getPointSize() * shortSize).order(ByteOrder.nativeOrder())
            .order(ByteOrder.nativeOrder())
        for (i in 0 until getPointSize()){
            indexData.putShort(i.toShort())
        }
        indexData.flip()
        indexData
    }

    fun getPointSize(): Int {
        return pointList.size
    }

    fun getOffsetStripSize(): Int {
        return 3 * floatSize
    }

    fun getStripSize(): Int{
        return 7 * floatSize
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NormalPoint

        if (pointList != other.pointList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pointList.hashCode()
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