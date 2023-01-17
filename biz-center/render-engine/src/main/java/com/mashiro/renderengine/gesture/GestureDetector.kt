package com.mashiro.renderengine.gesture

import android.view.MotionEvent
import android.view.View
import com.mashiro.renderengine.engine.impl.EngineConfig

enum class ViewGesture { START, ORBIT, PAN, ZOOM, END }

abstract class GestureDetector(
    private val view: View,
    var engineConfig: EngineConfig,
) {

    var currentViewGesture = ViewGesture.END
    protected var previousTouch = TouchPair()
    var currentScale = engineConfig.defaultScale
    var updateProjection: (()->Unit)? = null

    fun onTouchEvent(event: MotionEvent) {
        val touch = TouchPair(event, view.height)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                currentViewGesture = ViewGesture.START
            }
            MotionEvent.ACTION_MOVE -> {
                // 处理异常手势
                if (ILegalGesture(event)) {
                    endGesture()
                    return
                }

                // 更新现存手势
                // 返回true代表不执行下面创建手势操作
                // 返回false表示需要创建新手势
                if (updateExistGesture(touch)){
                    previousTouch = touch
                    return
                }

                // 新建手势
                if (isOrbitGesture(event, touch)) {
                    operateOrbitGesture(touch)
                    previousTouch = touch
                    return
                }
                if (isZoomGesture(event, touch)) {
                    operateZoomGesture(touch)
                    previousTouch = touch
                    return
                }
                if (isPanGesture(event, touch)) {
                    operatePanGesture(touch)
                    previousTouch = touch
                    return
                }
                previousTouch = touch
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                endGesture()
            }
        }
    }

    abstract fun ILegalGesture(event: MotionEvent): Boolean

    open fun endGesture() {
        currentViewGesture = ViewGesture.END
        previousTouch = TouchPair()
    }

    abstract fun updateExistGesture(touch: TouchPair): Boolean

    /**
     * 旋转手势
     */
    abstract fun isOrbitGesture(event: MotionEvent, touch: TouchPair): Boolean

    open fun operateOrbitGesture(touch: TouchPair) {
        currentViewGesture = ViewGesture.ORBIT
    }

    /**
     * 平移
     */
    abstract fun isPanGesture(event: MotionEvent, touch: TouchPair): Boolean

    open fun operatePanGesture(touch: TouchPair) {
        currentViewGesture = ViewGesture.PAN
    }


    /**
     * 放大
     */
    abstract fun isZoomGesture(event: MotionEvent, touch: TouchPair): Boolean

    open fun operateZoomGesture(touch: TouchPair) {
        currentViewGesture = ViewGesture.ZOOM
    }
}