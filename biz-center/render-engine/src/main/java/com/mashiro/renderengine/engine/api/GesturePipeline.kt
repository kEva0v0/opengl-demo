package com.mashiro.renderengine.engine.api

import android.view.MotionEvent
import android.view.View
import com.mashiro.renderengine.gesture.GestureType
import com.mashiro.renderengine.gesture.GestureDetector
import com.mashiro.renderengine.gesture.FilamentGestureListener

interface GesturePipeline: CameraPipeline, View.OnTouchListener {
    fun setGestureType(gestureType: GestureType)
    fun setGesture(gesture: GestureDetector)
    fun addGestureListener(gestureListener: FilamentGestureListener)
    fun removeGestureListener(gestureListener: FilamentGestureListener)
    fun invokeListeners(event: MotionEvent)
    fun releaseGesture()
}