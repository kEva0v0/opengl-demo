package com.mashiro.filament.viewmodel

import android.content.Context
import com.mashiro.filament.model.NormalPoint
import com.mashiro.filament.model.Point
import com.mashiro.filament.utils.FileUtils
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader

class PointCloudModel {

    companion object {
        private const val POINT_CLOUD_FOLDER_NAME = "pointcloud"
        private const val POINT_CLOUD_FILE_NAME = "point_cloud.ply"
    }

    fun getPointCloudData(): NormalPoint {
        return createFromInputStream(FileInputStream(getFilePath()))
    }

    private fun getFilePath() = "$POINT_CLOUD_FOLDER_NAME/$POINT_CLOUD_FILE_NAME"

    private fun createFromInputStream(inputStream: InputStream): NormalPoint {
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