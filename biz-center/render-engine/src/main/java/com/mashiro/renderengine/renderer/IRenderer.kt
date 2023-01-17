package com.mashiro.renderengine.renderer

import android.content.Context
import com.mashiro.renderengine.geometries.BaseGeometry
import com.google.android.filament.Engine
import com.google.android.filament.Scene

interface IRenderer {

    /**
     * 绑定引擎
     */
    fun bindEngine(entity: Int, engine: Engine, scene: Scene)

    /**
     * 加载纹理文件
     */
    fun loadMaterial(context: Context)

    /**
     * 绘制
     */
    fun rebuild(model: BaseGeometry)

    fun destroyRenderer()
}