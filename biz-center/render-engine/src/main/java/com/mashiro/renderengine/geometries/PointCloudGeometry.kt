package com.mashiro.renderengine.geometries

import com.mashiro.renderengine.model.PointCloud
import com.mashiro.renderengine.model.Vertex

class PointCloudGeometry(
    val pointCloud: PointCloud
): BaseGeometry() {

    override fun initPositionList(): List<Float> {
        return pointCloud.positions
    }

    override fun initColorList(): List<Float> {
        return pointCloud.colors
    }
}