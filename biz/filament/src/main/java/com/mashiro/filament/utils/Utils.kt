package com.mashiro.filament.utils

import com.mashiro.filament.model.Point
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

fun FloatArray.toFloatBuffer(): FloatBuffer {
    return ByteBuffer.allocateDirect(this.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(this@toFloatBuffer)
        }
}

fun ByteBuffer.put(v: Point): ByteBuffer {
    putFloat(v.x)
    putFloat(v.y)
    putFloat(v.z)
    return this
}