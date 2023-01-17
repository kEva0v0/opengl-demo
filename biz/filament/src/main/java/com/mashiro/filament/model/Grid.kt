package com.mashiro.filament.model

import android.graphics.Color
import com.mashiro.filament.utils.put
import com.mashiro.filament.utils.toFloatBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Grid {
    val boundSize = 3f
    // 这里要保证平面在水平以下，不会遮挡住z轴
    val pointList = listOf(
        // +X, +Z quadrant
        Point(boundSize, -1f, boundSize),
        Point(-boundSize, -1f, boundSize),
        Point(boundSize, -1f, -boundSize),
        Point(-boundSize, -1f, -boundSize)
    )
    val floorColor = floatArrayOf(34f / 255, 37f / 255, 42f / 255, 1f)
    val lineColor = floatArrayOf(1f, 1f, 1f, 0.1f)

    val vertexFloatBuffer by lazy {
        val vertexData = ByteBuffer.allocate(pointList.size * 12)
            // It is important to respect the native byte order
            .order(ByteOrder.nativeOrder())
//            .put(Point(boundSize, 0.0f,boundSize))
//            .put(Point(-boundSize, 0.0f, boundSize))
//            .put(Point(boundSize, 0.0f, -boundSize))
//            .put(Point(-boundSize, 0.0f, -boundSize))
            .put(Point(-boundSize, 0f, boundSize))
            .put(Point(boundSize,0f, boundSize))

            .put(Point(-boundSize, 0f, -boundSize))
            .put(Point(boundSize, 0f, -boundSize))
            .flip()
        vertexData
    }

    val indexData: ByteBuffer by lazy {
        val indexData = ByteBuffer.allocate(pointList.size * NormalPoint.shortSize)
            .order(ByteOrder.nativeOrder())
        for (i in pointList.indices){
            indexData.putShort(i.toShort())
        }
        indexData.flip()
        indexData
    }

}