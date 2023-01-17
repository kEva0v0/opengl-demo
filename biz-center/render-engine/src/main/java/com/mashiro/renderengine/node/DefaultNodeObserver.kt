package com.mashiro.renderengine.node

import android.util.Log
import com.mashiro.renderengine.Constants

class DefaultNodeObserver: NodeObserver {
    override fun onState(node: Node, nodeStatus: NodeStatus) {
        Log.d(Constants.TAG, "node:${node.entity} status is $nodeStatus")
    }
}