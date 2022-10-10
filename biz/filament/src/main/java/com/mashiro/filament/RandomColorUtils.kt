package com.mashiro.filament

import android.graphics.Color
import java.util.*

object RandomColorUtils {
    fun getRandomColor(): Int{
        val random = Random()
        val r = random.nextFloat() * 255
        val g = random.nextFloat() * 255
        val b = random.nextFloat() * 255
        return Color.argb(255, r.toInt(), g.toInt(), b.toInt())
//        return Color.GREEN
    }
}