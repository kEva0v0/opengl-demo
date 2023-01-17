package com.mashiro.renderengine.node.component

import com.mashiro.renderengine.math.Entity
import com.mashiro.renderengine.math.Transform

/**
 * 关系组件
 * 表征一个节点的父子关系
 */
interface RelationComponent: Component {

    val transformManager get() = engine.transformManager
    val transformInstance: Int get() = transformManager.getInstance(entity)

    fun createComponent(defaultTransform: Transform? = null): Int {
        return transformManager.create(entity)
    }

    fun hasComponent(): Boolean {
        return transformManager.hasComponent(entity)
    }

    fun getParent(): Entity?{
        return transformManager.getParent(transformInstance).takeIf { it != 0 }
    }

    fun getParentInstance(): Int? {
        return getParent()?.let { transformManager.getInstance(it) }
    }

    fun setParent(parentEntity: Entity?){
        transformManager.setParent(transformInstance, parentEntity?.let {
            transformManager.getInstance(parentEntity)
        } ?: 0)
    }

    fun getChildren(): List<Entity>{
        return transformManager.getChildren(transformInstance, null).toList()
    }


}