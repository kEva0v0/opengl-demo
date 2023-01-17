package com.mashiro.renderengine.node.component

interface RenderableComponent: Component {
    fun reBuild()
    fun finishBuild()
}