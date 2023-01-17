package com.mashiro.renderengine.model

import com.mashiro.renderengine.geometries.PointCloudGeometry
import com.mashiro.renderengine.renderer.PointCloudRender

data class PointCloud(
    val positions: List<Float>,
    val colors: List<Float>
): BaseModel<PointCloudGeometry, PointCloudRender> {
    override fun getGeometry(): PointCloudGeometry {
        return PointCloudGeometry(this)
    }

    override fun getRenderer(): PointCloudRender {
        return PointCloudRender()
    }

}