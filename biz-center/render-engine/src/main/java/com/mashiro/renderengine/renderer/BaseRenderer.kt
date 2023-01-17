package com.mashiro.renderengine.renderer

import android.content.Context
import com.mashiro.renderengine.geometries.BaseGeometry
import com.mashiro.renderengine.utils.AssetUtils
import com.google.android.filament.Engine
import com.google.android.filament.Material
import com.google.android.filament.Scene

/**
 * 每个geometry对应的renderer
 * 如果需要新增geometry，请务必实现对应的renderer并与对应的Geometry绘制方式对应
 * @see PointCloudRender
 */
abstract class BaseRenderer {

    protected var entity: Int? = null
    protected var material: Material? = null
    protected var engine: Engine? = null
    protected var scene: Scene? = null

    open fun bindEngine(entity: Int, engine: Engine, scene: Scene) {
        this.entity = entity
        this.engine = engine
        this.scene = scene
    }

    open fun loadMaterial(context: Context) {
        if(engine != null){
            AssetUtils.readUncompressedAsset(context, getMaterialPath()).let {
                material = Material.Builder().payload(it, it.remaining()).build(engine!!)
            }
        }
    }

    abstract fun getMaterialPath(): String

    /**
     * 绘制
     */
    abstract fun rebuild(model: BaseGeometry)
    
    open fun destroyRenderer() {
        if (engine != null && entity != null && material != null) {
            engine!!.transformManager.destroy(entity!!)
            engine!!.renderableManager.destroy(entity!!)
            engine!!.destroyMaterial(material!!)
        }
    }
}