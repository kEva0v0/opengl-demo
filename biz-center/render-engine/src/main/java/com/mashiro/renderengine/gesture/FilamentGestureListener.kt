package com.mashiro.renderengine.gesture

import android.view.MotionEvent

interface FilamentGestureListener {
    fun onClick(event: MotionEvent)
    fun onMove(event: MotionEvent)
    fun onScroll(event: MotionEvent)
}