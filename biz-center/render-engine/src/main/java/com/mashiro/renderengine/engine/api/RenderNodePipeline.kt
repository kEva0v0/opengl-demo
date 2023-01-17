package com.mashiro.renderengine.engine.api

import com.mashiro.renderengine.geometries.BaseGeometry
import com.mashiro.renderengine.math.Entity
import com.mashiro.renderengine.model.BaseModel
import com.mashiro.renderengine.node.Node
import com.mashiro.renderengine.node.NodeObserver
import com.mashiro.renderengine.renderer.BaseRenderer

interface RenderNodePipeline: Pipeline {
    fun addNode(model: BaseModel<*,*>, observer: NodeObserver?, autoRender: Boolean): Node?
    fun bindParent(childNodeEntity: Entity, parentNodeEntity: Entity)
    fun removeNode(nodeEntity: Entity, autoRender: Boolean)
    fun updateNode(nodeEntity: Entity, autoRender: Boolean, func: (Node)->Unit)
    fun getNode(nodeEntity: Entity): Node?

    fun rebuild()
    fun finishBuild()
    fun destroyNodes()
}