package com.mashiro.renderengine.engine.impl


import com.mashiro.renderengine.engine.api.RenderNodePipeline
import com.mashiro.renderengine.geometries.BaseGeometry
import com.mashiro.renderengine.math.Entity
import com.mashiro.renderengine.model.BaseModel
import com.mashiro.renderengine.node.Node
import com.mashiro.renderengine.node.NodeObserver
import com.mashiro.renderengine.node.NodeStatus
import com.mashiro.renderengine.node.RendererNode
import com.mashiro.renderengine.renderer.BaseRenderer
import com.google.android.filament.Engine
import com.google.android.filament.Scene
import java.util.concurrent.ConcurrentHashMap

class RenderNodePipelineImpl(
    private val engine: Engine,
    private val scene: Scene,
    override val engineConfig: EngineConfig
): RenderNodePipeline {

    private val nodeMap: ConcurrentHashMap<Entity, Node> = ConcurrentHashMap()

    override fun addNode(
        model: BaseModel<*,*>,
        observer: NodeObserver?,
        autoRender: Boolean
    ): Node? {
        RendererNode.Builder()
            .bindEngine(engine)
            .bindScene(scene)
            .bindObserver(observer)
            .bindGeometry(model.getGeometry())
            .bindRenderer(model.getRenderer())
            .build()
            ?.let {
                nodeMap[it.entity] = it
                return it
            }
        return null
    }


    // 这里需要避免成环
    override fun bindParent(childNodeEntity: Entity, parentNodeEntity: Entity) {
        if (nodeMap.containsKey(childNodeEntity) && nodeMap.containsKey(parentNodeEntity) && checkIfLoop(childNodeEntity, parentNodeEntity)){
            nodeMap[childNodeEntity]!!.parentNode = parentNodeEntity
            nodeMap[parentNodeEntity]!!.addChild(childNodeEntity)
        }
    }

    // 判断当前Entity的子孙中是否包含target
    private fun checkIfLoop(currentNodeEntity: Entity, targetEntity: Entity): Boolean {
        if (currentNodeEntity == targetEntity) {
            return true
        }
        nodeMap[currentNodeEntity]?.takeIf { it.getChildrenNodes().isNotEmpty() }?.let { parent ->
            var currentState = false
            parent.getChildrenNodes().forEach { childEntity ->
                currentState = currentState or checkIfLoop(childEntity, targetEntity)
            }
            return currentState
        }
        return false
    }

    override fun removeNode(nodeEntity: Entity, autoRender: Boolean) {
        if (!nodeMap.containsKey(nodeEntity)){
            return
        }
        val node = nodeMap.remove(nodeEntity)
        // 先销毁
        node!!.destroy()
        // 再销毁父子关系
        node.parentNode?.let {  nodeMap[it]?.removeChild(nodeEntity) }
        // 成环也不怕，hhh
        node.getChildrenNodes().forEach {
            removeNode(it, autoRender)
        }
        node.parentNode = null
        node.removeAllChild()
    }

    override fun  updateNode(nodeEntity: Entity, autoRender: Boolean, func: (Node) -> Unit) {
        nodeMap[nodeEntity]?.let{ parentNode ->
            func(parentNode)
            // 把所有儿子当作dirty
            loopChildren(nodeEntity){
                it.markNeedsBuild()
            }
        }
    }

    private fun loopChildren(currentNodeEntity: Entity, func: (Node) -> Unit){
        nodeMap[currentNodeEntity]?.let { currentNode ->
            func(currentNode)
            currentNode.getChildrenNodes().forEach {
                loopChildren(it, func)
            }
        }
    }

    override fun  getNode(nodeEntity: Entity): Node? {
        return (nodeMap[nodeEntity])
    }

    override fun rebuild() {
        nodeMap.filter { it.value.nodeStatus == NodeStatus.DIRTY }.forEach {
            it.value.reBuild()
        }
    }

    override fun finishBuild() {
        nodeMap.filter { it.value.nodeStatus == NodeStatus.RENDERING }.forEach  {
            it.value.finishBuild()
        }
    }

    override fun destroyNodes() {
        nodeMap.forEach {
            it.value.destroy()
        }
    }

}