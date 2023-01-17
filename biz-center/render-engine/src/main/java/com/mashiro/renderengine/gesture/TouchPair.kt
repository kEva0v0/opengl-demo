package com.mashiro.renderengine.gesture

import android.view.MotionEvent
import com.google.android.filament.utils.Float2
import com.google.android.filament.utils.distance
import com.google.android.filament.utils.mix

data class TouchPair(var pt0: Float2, var pt1: Float2, var count: Int) {
    constructor() : this(Float2(0f), Float2(0f), 0)
    constructor(me: MotionEvent, height: Int) : this() {
        if (me.pointerCount >= 1) {
            this.pt0 = Float2(me.getX(0), height - me.getY(0))
            this.pt1 = this.pt0
            this.count++
        }
        if (me.pointerCount >= 2) {
            this.pt1 = Float2(me.getX(1), height - me.getY(1))
            this.count++
        }
    }
    val separation get() = distance(pt0, pt1)
    val midpoint get() = mix(pt0, pt1, 0.5f)
    val x: Int get() = midpoint.x.toInt()
    val y: Int get() = midpoint.y.toInt()
    fun isZero(): Boolean {
        return x == 0 && y == 0
    }
}