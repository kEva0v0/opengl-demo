package com.mashiro.renderengine.model

import com.mashiro.renderengine.geometries.BaseGeometry
import com.mashiro.renderengine.renderer.BaseRenderer

interface BaseModel<Geometry: BaseGeometry, Renderer: BaseRenderer> {
    fun getGeometry(): Geometry
    fun getRenderer(): Renderer
}