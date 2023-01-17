package com.mashiro.renderengine.node

interface NodeObserver {
    fun onState(node: Node, nodeStatus: NodeStatus)
}