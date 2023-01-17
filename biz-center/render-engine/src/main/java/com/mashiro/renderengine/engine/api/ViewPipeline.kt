package com.mashiro.renderengine.engine.api

import android.view.View
import com.mashiro.renderengine.math.Entity
import com.mashiro.renderengine.math.Position

/**
 * 封装一些与Android View配合的组件
 */
interface ViewPipeline {

    /**
     * 将native-view移动距离反馈在entity对应的Node上
     */
    fun moveNodeWithNativeView(entity: Entity, distance: Position)

    /**
     * 根据物体对应的世界坐标，调整android view的中心点与其对齐
     */
    fun adjustNativeViewWithCentroid(entity: Entity, view: View?)
}