package com.mashiro.renderengine.model

import com.mashiro.renderengine.math.*


data class Plane(
    val center: Position,
    // Normal
    val direction: Direction,
    val width: Float,
    val height: Float
)
