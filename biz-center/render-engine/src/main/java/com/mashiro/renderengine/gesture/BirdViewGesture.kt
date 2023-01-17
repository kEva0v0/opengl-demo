package com.mashiro.renderengine.gesture

import android.view.MotionEvent
import android.view.View
import com.mashiro.renderengine.engine.impl.EngineConfig
import com.google.android.filament.Camera

import com.google.android.filament.utils.Manipulator

class BirdViewGesture(private val view: View, private val manipulator: Manipulator, engineConfig: EngineConfig) : GestureDetector(view, engineConfig) {

    override fun ILegalGesture(event: MotionEvent): Boolean {
        return (event.pointerCount != 1 && currentViewGesture == ViewGesture.PAN) ||
                (event.pointerCount != 2 && currentViewGesture == ViewGesture.ZOOM)
    }

    override fun updateExistGesture(touch: TouchPair): Boolean {
        if (currentViewGesture == ViewGesture.ZOOM) {
            val d0 = previousTouch.separation
            val d1 = touch.separation
            val currentGestureScale = currentScale * d0 / d1
            if (currentGestureScale > engineConfig.maxScale || currentGestureScale < engineConfig.minScale) {
                return true
            }
            currentScale = currentGestureScale
            when(engineConfig.cameraProjection){
                Camera.Projection.PERSPECTIVE -> {
                    manipulator.scroll(touch.x, touch.y, (d0 - d1) * engineConfig.zoomSpeed)
                }
                Camera.Projection.ORTHO -> {
                    updateProjection?.invoke()
                }
            }
            return true
        }

        if (currentViewGesture != ViewGesture.START && currentViewGesture != ViewGesture.END) {
            manipulator.grabUpdate(touch.x, touch.y)
            return true
        }
        return false
    }

    // 鸟瞰图不允许旋转相机
    override fun isOrbitGesture(event: MotionEvent, touch: TouchPair): Boolean {
        return false
    }

    override fun isPanGesture(event: MotionEvent, touch: TouchPair): Boolean {
        return event.pointerCount == 1
    }
    // 单指拖动
    override fun operatePanGesture(touch: TouchPair) {
        super.operatePanGesture(touch)
        manipulator.grabBegin(touch.x, touch.y, true)
    }

    override fun isZoomGesture(event: MotionEvent, touch: TouchPair): Boolean {
        return event.pointerCount == 2
    }

    override fun endGesture() {
        super.endGesture()
        manipulator.grabEnd()
    }
}