package com.mashiro.filament.mark

import android.view.MotionEvent

interface IMarkStateListener {
    fun onSelected()
    fun onTouchEvent(event: MotionEvent)
}