package com.mashiro.renderengine.node

import android.content.Context
import android.util.Log
import com.mashiro.renderengine.Constants
import com.mashiro.renderengine.geometries.BaseGeometry
import com.mashiro.renderengine.math.*
import com.mashiro.renderengine.node.component.GeometryComponent
import com.mashiro.renderengine.node.component.RenderableComponent
import com.mashiro.renderengine.node.component.TransformComponent
import com.mashiro.renderengine.renderer.BaseRenderer
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Scene

/**
 *
 */
abstract class Node(
    override val engine: Engine,
    override val scene: Scene,
    private val observer: NodeObserver? = DefaultNodeObserver()
): TransformComponent, RenderableComponent, GeometryComponent{

    companion object {
        private const val MODEL_NEEDS_BUILD = 0x2
        private const val NORMAL_BUILD = 0x1
    }

    override val entity: Entity = EntityManager.get().create()

    var nodeStatus = NodeStatus.CREATING
        private set
    var parentNode: Entity? = null
        set(value) {
            if (value != null) {
                transformManager.setParent(entity, value)
            }
            field = value
        }
    private val childrenNode: MutableList<Entity> = mutableListOf()

    var renderer: BaseRenderer? = null
    var geometry: BaseGeometry? = null

    open fun init(
        context: Context,
        position: Position = Position(0f,0f,0f),
        rotation: Rotation = Rotation(0f,0f,0f),
        scale: Scale = Scale(1f,1f,1f)
    ) {
        nodeStatus = NodeStatus.PENDING
        geometry?.init(engine)
        renderer?.bindEngine(entity, engine, scene)
        renderer?.loadMaterial(context)
        // 创建scene
        scene.addEntity(entity)
        // 创建transform
        if (!hasComponent()) {
            createComponent()
        }
        transform(position, rotation, scale)
        observer?.onState(this, nodeStatus)
        markNeedsBuild()
    }

    open fun destroy() {
        observer?.onState(this, NodeStatus.DESTROY)
        // 销毁buffer和vertex
        geometry?.destroyGeometry()
        renderer?.destroyRenderer()
        // 销毁入口
        scene.removeEntity(entity)
        transformManager.destroy(entity)
        engine.destroyEntity(entity)
        // 清空数据
        geometry = null
        renderer = null
        parentNode = null
        childrenNode.clear()
    }

    /**
     * Node节点管理
     */
    fun isRoot(): Boolean { return parentNode == null }

    fun getChildrenNodes(): List<Entity> = childrenNode

    fun addChild(node: Entity) { childrenNode.add(node) }

    fun removeChild(node: Entity){ childrenNode.remove(node) }

    fun removeAllChild() { childrenNode.clear() }

    protected fun canBuild(): Boolean = nodeStatus == NodeStatus.PENDING

    // 非pending不可改变状态
    // 注意这里要分等级
    fun markNeedsBuild(){
        if (canBuild()){
            nodeStatus = NodeStatus.DIRTY
            observer?.onState(this, nodeStatus)
        }
    }

    /**
     * Transform Component实现区域
     * @see currentTransform
     * @see addTransform
     */
    override fun transform(position: Position, rotation: Rotation, scale: Scale) {
        if (canBuild()) {
            observer?.onState(this, nodeStatus)
            super.transform(position, rotation, scale)
            markNeedsBuild()
        }
    }

    override fun addTransform(position: Position, rotation: Rotation, scale: Scale) {
        if (canBuild()) {
            observer?.onState(this, nodeStatus)
            super.addTransform(position, rotation, scale)
            markNeedsBuild()
        }
    }

    /**
     * RenderableComponent实现区域
     * @see reBuild
     * @see finishBuild
     */
    // 只有在dirty下才操作
    override fun reBuild() {
        if (nodeStatus == NodeStatus.DIRTY){
            Log.d(Constants.TAG_STEP, "Node rebuilding")
            nodeStatus = NodeStatus.RENDERING
            // TODO:这里是不是最好不要在主线程做这件事
            observer?.onState(this, nodeStatus)
            // 确定是改动到了model
            if (geometry != null && geometry?.isNeedBuild() == true) {
                Log.d(Constants.TAG_STEP, "Node need update geometry")
                renderer?.rebuild(geometry!!)
            }
        }
    }

    override fun finishBuild() {
        if (nodeStatus == NodeStatus.RENDERING) {
            geometry?.finishBuild()
            nodeStatus = NodeStatus.PENDING
        }
    }

    /**
     * GeometryComponent实现区域
     * @see getModelCentroid
     * @see getWorldCentroid
     */
    override fun getModelCentroid(): Position? {
        if (geometry == null){
            return null
        }
        return geometry!!.getCentroid()
    }

    override fun getWorldCentroid(): Position? {
        if (geometry == null){
            return null
        }
        return worldTransform*getModelCentroid()!!
    }
}