package com.mashiro.uitest.testspan

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.util.Log
import android.view.View

class LoadingIndicatorImageSpan(val context: Context, val anchorView: View?, resId: Int) : DynamicDrawableSpan() {

    private var drawableRef: LoadingDrawable = LoadingDrawable(context)
    private var alphaList = mutableListOf<Int>((255 * 0.2).toInt(), (255 * 0.6).toInt(), 255)

    private val evaluator = PersonalEvaluator(0.2f, 1f, 11)

    private val animator = ValueAnimator.ofInt(0, 12).apply {
        duration = 800L
        repeatCount = -1
        repeatMode = ValueAnimator.RESTART
        addUpdateListener {
            drawableRef.setStep((it.animatedValue as Int))
            anchorView?.invalidate()
        }
    }


    var needAnim = false
        set(value) {
            field = value
            if (!value) {
                cancelAnim()
            }
        }

    fun startAnim() {
        if (needAnim) {
            animator.start()
        }
    }

    fun cancelAnim() {
        animator.cancel()
    }

    override fun getDrawable(): Drawable = drawableRef

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return super.getSize(paint, text, start, end, fm) + dp2px(4f).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val d = drawable ?: return
        val fm = paint.fontMetricsInt
        val transY = (y + fm.descent + y + fm.ascent) / 2 - d.bounds.bottom / 2
        canvas.save()
        canvas.translate(x, transY.toFloat())
        d.draw(canvas)
        canvas.restore()
    }
    
    private fun dp2px(dp: Float): Float{
        val density = context.resources.displayMetrics.density
        return dp * density + 0.5f
    }
}