package com.mashiro.filament.model

import android.graphics.Color
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class NormalPoint(
    val pointList: MutableList<Point>,
    val vertexMatrix: FloatArray = floatArrayOf(1f,0f,0f,0f,0f,1f,0f,0f,0f,0f,1f,0f,0f,0f,0f,1f)
){
    companion object {
        const val POSITION_COLLECT_COUNT = 2
        const val POSITION_COMPONENT_COUNT = 4
        const val MAX_SIZE = 150000

        const val intSize = 4
        const val floatSize = 4
        const val shortSize = 2

        fun createFromInputStream(inputStream: InputStream): NormalPoint {
            val pointList = mutableListOf<Point>()
            inputStream.use {
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                var vertexCount = 0
                var indexCount = 0
                var xIndex = -1
                var yIndex = -1
                var zIndex = -1
                var rIndex = -1
                var gIndex = -1
                var bIndex = -1
                var aIndex = -1
                var line: String
                var parts: List<String>
                var x: Float
                var y: Float
                var z: Float
                var r: Int
                var g: Int
                var b: Int
                var a: Int
                while (bufferedReader.readLine().also { line = it.orEmpty().trim() } != null) {
                    parts = line.split(" ")
                    if (line.startsWith("element vertex")) {
                        vertexCount = parts.last().toInt()
                    } else if (line.startsWith("property")) {
                        when (parts.last()) {
                            "x" -> xIndex = indexCount
                            "y" -> yIndex = indexCount
                            "z" -> zIndex = indexCount
                            "red" -> rIndex = indexCount
                            "green" -> gIndex = indexCount
                            "blue" -> bIndex = indexCount
                            "alpha" -> aIndex = indexCount
                        }
                        indexCount++
                    } else if (line.startsWith("end_header")) {
                        break
                    }
                }
                for (i in 0 until vertexCount) {
                    line = bufferedReader.readLine().trim()
                    parts = line.split(" ")
                    x = if (xIndex != -1) parts[xIndex].toFloat() else 0f
                    // z和y相反
                    y = if (yIndex != -1) parts[yIndex].toFloat() else 0f
                    z = if (zIndex != -1) parts[zIndex].toFloat() else 0f
                    r = if (rIndex != -1) parts[rIndex].toInt() else 255
                    g = if (gIndex != -1) parts[gIndex].toInt() else 255
                    b = if (bIndex != -1) parts[bIndex].toInt() else 255
                    a = if (aIndex != -1) parts[aIndex].toInt() else 255
                    pointList += Point(x, y, z, r, g, b, a)
                }
            }
            return NormalPoint(pointList)
        }

    }

    val vertexData: ByteBuffer by lazy {
        val byteBuffer = ByteBuffer.allocate(getPointSize()*(getXYZDataSize()+getColorSize())).order(ByteOrder.nativeOrder())
        pointList.forEachIndexed { index, point ->
            byteBuffer.putFloat(point.x)
            byteBuffer.putFloat(point.y)
            byteBuffer.putFloat(point.z)
            // TODO：这里为啥是反的？？
            val color = Color.argb(point.a,point.b,point.g,point.r)
            byteBuffer.putInt(color)
        }
        byteBuffer.flip()
        byteBuffer
    }

    val vertexColor: ByteBuffer by lazy {
        val byteBuffer = ByteBuffer.allocate(getPointSize() * getColorSize()).order(ByteOrder.nativeOrder())
        pointList.forEachIndexed { index, point ->
            val color = Color.argb(point.a,point.r,point.g,point.b)
            byteBuffer.putInt(color)
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

    fun getXYZDataSize(): Int {
        return 3 * floatSize
    }

    fun getColorSize(): Int {
        return intSize
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


    fun minZ(): Float {
        return pointList.minOf { it.z }
    }


}