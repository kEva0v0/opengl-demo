package com.mashiro.filament.model

data class Point(
    var x: Float,
    var y: Float,
    var z: Float,
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int,
) {
    constructor(x: Float, y: Float, z: Float) : this(x, y, z, 0, 0, 0, 1)
}