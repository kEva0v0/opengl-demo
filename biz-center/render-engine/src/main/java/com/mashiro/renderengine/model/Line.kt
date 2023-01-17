package com.mashiro.renderengine.model

import com.mashiro.renderengine.math.*

data class Line(
    val start: Position,
    val end: Position,
    val color: RenderColor?
)
