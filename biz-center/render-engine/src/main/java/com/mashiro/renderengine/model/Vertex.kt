package com.mashiro.renderengine.model

import androidx.annotation.FloatRange

/**
 * 平铺改造，防止JVM创建过多对象导致OOM
 */
data class Vertex(
    val positionX: Float,
    val positionY: Float,
    val positionZ: Float,
    val directionX: Float? = null,
    val directionY: Float? = null,
    val directionZ: Float? = null,
    val uvCoordinateX: Float? = null,
    val uvCoordinateY: Float? = null,
    @FloatRange(from = 0.0, to = 1.0) val red:  Float? = 1f,
    @FloatRange(from = 0.0, to = 1.0) val green: Float? = 1f,
    @FloatRange(from = 0.0, to = 1.0) val blue: Float? = 1f,
    @FloatRange(from = 0.0, to = 1.0) val alpha: Float? = 1f
)