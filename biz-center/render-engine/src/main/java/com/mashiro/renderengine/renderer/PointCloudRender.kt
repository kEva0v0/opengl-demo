package com.mashiro.renderengine.renderer

import com.mashiro.renderengine.geometries.BaseGeometry
import com.mashiro.renderengine.model.PointCloud
import com.google.android.filament.RenderableManager
import dev.romainguy.kotlin.math.transform

class PointCloudRender: BaseRenderer() {

    override fun getMaterialPath(): String = "materials/baked_color.filamat"

    override fun rebuild(model: BaseGeometry) {
        if (entity != null && engine != null) {
            RenderableManager.Builder(1).apply {
                // 绑定vertex
                geometry(0, RenderableManager.PrimitiveType.POINTS, model.vertexBuffer, model.indexBuffer)
                boundingBox(model.boundingBox)
                // 绑定material
                if (material != null) {
                    material(0, material!!.defaultInstance)
                }
                build(engine!!, entity!!)
            }
        }
    }

}