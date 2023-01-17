package com.mashiro.renderengine.engine.impl

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import com.mashiro.renderengine.Constants

import com.mashiro.renderengine.engine.api.GesturePipeline
import com.mashiro.renderengine.gesture.*
import com.mashiro.renderengine.math.Position
import com.google.android.filament.Camera
import com.google.android.filament.utils.Manipulator

class GesturePipelineImpl(
    private val surfaceView: SurfaceView,
    override val engineConfig: EngineConfig,
    override val camera: Camera,
    override val view: com.google.android.filament.View
): GesturePipeline {

    override var cameraManipulator = Manipulator.Builder()
        .targetPosition(
            engineConfig.targetPosition.x,
            engineConfig.targetPosition.y,
            engineConfig.targetPosition.z
        )
        .upVector(
            engineConfig.upVector.x,
            engineConfig.upVector.y,
            engineConfig.upVector.z
        )
        .orbitHomePosition(
            engineConfig.cameraPosition.x,
            engineConfig.cameraPosition.y,
            engineConfig.cameraPosition.z
        )
        .viewport(surfaceView.width, surfaceView.height)
        .orbitSpeed(
            engineConfig.orbitSpeed.x,
            engineConfig.orbitSpeed.y
        )
        .zoomSpeed(engineConfig.zoomSpeed)
        .build(Manipulator.Mode.ORBIT)

    private var filamentGestureListener: MutableList<FilamentGestureListener> = mutableListOf()

    /**
     * 允许用户自定Gesture
     */
    private var gestureDetector: GestureDetector = BirdViewGesture(surfaceView, cameraManipulator, engineConfig).apply {
        this.updateProjection = { updateCameraProjection() }
    }

    private var mGestureType: GestureType = GestureType.BIRD_VIEW
        set(value) {
            field = value
            updateGestureType(value)
            gestureDetector.updateProjection =  { updateCameraProjection() }
        }

    private fun updateGestureType(gestureType: GestureType){
        when(gestureType){
            GestureType.BIRD_VIEW ->{
                gestureDetector = BirdViewGesture(surfaceView, cameraManipulator, engineConfig)
            }
            GestureType.THREE_DIMENSION_VIEW -> {
                gestureDetector = ThreeDimensionGestureDetector(surfaceView, cameraManipulator, engineConfig)
            }
            else -> {}
        }
    }

    override fun resetCameraManipulator(
        cameraPosition: Position,
        targetPosition: Position,
        upVector: Position
    ) {
        super.resetCameraManipulator(cameraPosition, targetPosition, upVector)
        updateGestureType(mGestureType)
    }

    override fun setCameraProjectType(projection: Camera.Projection) {
        this.gestureDetector.engineConfig.cameraProjection = projection
        super.setCameraProjectType(projection)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun invokeListeners(event: MotionEvent) {
        when(gestureDetector.currentViewGesture){
            ViewGesture.START -> {
                filamentGestureListener.forEach { it.onClick(event) }
            }
            ViewGesture.ZOOM -> {
                filamentGestureListener.forEach {  it.onScroll(event)  }
            }
            ViewGesture.ORBIT, ViewGesture.PAN -> {
                filamentGestureListener.forEach {  it.onMove(event)  }
            }
            else -> {}
        }
    }

    override fun getZoom(): Double {
        return gestureDetector.currentScale.toDouble()
    }

    override fun addGestureListener(gestureListener: FilamentGestureListener) {
        this.filamentGestureListener.add(gestureListener)
    }

    override fun removeGestureListener(gestureListener: FilamentGestureListener) {
        this.filamentGestureListener.remove(gestureListener)
    }

    override fun setGestureType(gestureType: GestureType) {
        this.mGestureType = gestureType
    }

    override fun setGesture(gesture: GestureDetector) {
        this.gestureDetector = gesture.apply {
            this.updateProjection = { updateCameraProjection() }
        }
    }

    override fun releaseGesture() {
        this.filamentGestureListener.clear()
    }
}