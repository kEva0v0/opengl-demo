package com.mashiro.renderengine.node.component

import com.mashiro.renderengine.math.Position

interface GeometryComponent {
    fun getModelCentroid(): Position?
    fun getWorldCentroid(): Position?
}