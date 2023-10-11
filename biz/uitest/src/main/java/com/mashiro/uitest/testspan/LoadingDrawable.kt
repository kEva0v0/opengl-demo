package com.mashiro.uitest.testspan

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable

class LoadingDrawable(val context: Context): Drawable() {

    companion object {
        private const val MARGIN_VERTICAL = 6f
        private const val MARGIN_BETWEEN = 4f
        private const val CIRCLE_RADIUS = 2f
    }

    private val evaluator = PersonalEvaluator(0.2f, 1f, 11)

    private var step = 0

    private val paint = Paint()

    private val createRect = Rect(
        0,
        0,
        (getMarginVertical() * 2 + getMarginBetween() * 2 + getCircleRadius() * 2 * 3).toInt(),
        (getCircleRadius() * 2).toInt()
    )

    init {
        setBounds(createRect)
        paint.isAntiAlias = true
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
    }

    fun setStep(step: Int) {
        this.step = step
    }

    private fun getMarginVertical() = dp2px(MARGIN_VERTICAL)

    private fun getMarginBetween() = dp2px(MARGIN_BETWEEN)

    private fun getCircleRadius() = dp2px(CIRCLE_RADIUS)
    
    override fun draw(canvas: Canvas) {
        var startX = createRect.left + getMarginVertical() + getCircleRadius()
        var startY = getCircleRadius()
        paint.alpha = evaluator.calculateAsAlpha((step + 11)%12)
        canvas.drawCircle(startX, startY, getCircleRadius(), paint)
        paint.alpha = evaluator.calculateAsAlpha((step + 7)%12)
        startX += getMarginBetween() + getCircleRadius() * 2
        canvas.drawCircle(startX, startY, getCircleRadius(), paint)
        paint.alpha = evaluator.calculateAsAlpha((step + 3)%12)
        startX += getMarginBetween() + getCircleRadius() * 2
        canvas.drawCircle(startX, startY, getCircleRadius(), paint)
    }

    override fun setAlpha(alpha: Int) {
        //
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        //
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    private fun dp2px(dp: Float): Float{
        val density = context.resources.displayMetrics.density
        return dp * density + 0.5f
    }
    
}