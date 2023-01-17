package com.mashiro.renderengine.gesture

import android.view.MotionEvent
import android.view.View
import com.mashiro.renderengine.engine.impl.EngineConfig
import com.mashiro.renderengine.utils.getLookAt
import com.google.android.filament.Camera
import com.google.android.filament.utils.Manipulator
import com.google.android.filament.utils.distance
import dev.romainguy.kotlin.math.angle
import kotlin.math.abs

class ThreeDimensionGestureDetector(private val view: View, private val manipulator: Manipulator, engineConfig: EngineConfig) : GestureDetector(view, engineConfig) {

    companion object {
        private const val ZOOM_CONFIDENCE_DISTANCE = 10
        private const val PAN_CONFIDENCE_DISTANCE = 4
    }

    private val pitchHelper = PitchHelper()

    override fun ILegalGesture(event: MotionEvent): Boolean {
        return (event.pointerCount != 1 && currentViewGesture == ViewGesture.ORBIT) ||
                (event.pointerCount != 2 && currentViewGesture == ViewGesture.PAN) ||
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
            if (currentViewGesture == ViewGesture.ORBIT) {
//                manipulator.getLookAt().let{ lookAt ->
//                    val eyetar = lookAt.eye - lookAt.target
//                    val curAngle = Math.toDegrees(angle(eyetar, engineConfig.correspondVector).toDouble())
//                    val moveAngle = pitchHelper.calculatePhi(curAngle.toFloat(), touch.y.toFloat(), engineConfig.orbitSpeed.y)
//                    if (moveAngle <= engineConfig.maxCameraAngle && moveAngle >= engineConfig.minCameraAngle) {
                        manipulator.grabUpdate(touch.x, touch.y)
//                    }
//                }
            } else {
                manipulator.grabUpdate(touch.x, touch.y)
            }
            return true
        }
        return false
    }

    override fun isOrbitGesture(event: MotionEvent, touch: TouchPair): Boolean {
        return event.pointerCount == 1
    }


    override fun operateOrbitGesture(touch: TouchPair) {
        super.operateOrbitGesture(touch)
        manipulator.grabBegin(touch.x, touch.y, false)
    }

    // 判断上次指间距 与 本次指间距 的距离来决定是平移还是放大
    override fun isPanGesture(event: MotionEvent, touch: TouchPair): Boolean {
        if (event.pointerCount != 2 || previousTouch.isZero()) {
            return false
        }
        val oldest = previousTouch.midpoint
        val newest = touch.midpoint
        return distance(oldest, newest) > PAN_CONFIDENCE_DISTANCE
    }

    override fun isZoomGesture(event: MotionEvent, touch: TouchPair): Boolean {
        if (event.pointerCount != 2 || previousTouch.isZero()) {
            return false
        }
        val oldest = previousTouch.separation
        val newest = touch.separation
        return abs(oldest - newest) > ZOOM_CONFIDENCE_DISTANCE
    }

    // 拖动
    override fun operatePanGesture(touch: TouchPair) {
        super.operatePanGesture(touch)
        manipulator.grabBegin(touch.x, touch.y, true)
        pitchHelper.init(touch.y.toFloat())
    }

    override fun endGesture() {
        super.endGesture()
        manipulator.grabEnd()
        pitchHelper.release()
    }
}