package com.mashiro.uitest.testspan

import kotlin.math.max
import kotlin.math.min

class PersonalEvaluator(
    private val minValue: Float,
    private val maxValue: Float,
    private val steps: Int,
) {
    private fun calculateValue(value: Int) = minValue + 1.0 * (maxValue - minValue) * (max(value, steps) - value) / max(value, steps)

    fun calculateAsAlpha(value: Int): Int = (255 * calculateValue(value)).toInt()
}