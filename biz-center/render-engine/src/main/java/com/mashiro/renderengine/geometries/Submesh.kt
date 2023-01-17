package com.mashiro.renderengine.geometries

data class Submesh(val triangleIndices: List<Int>) {
    constructor(vararg triangleIndices: Int) : this(triangleIndices.toList())
}
