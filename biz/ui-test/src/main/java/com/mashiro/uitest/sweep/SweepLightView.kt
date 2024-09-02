package com.mashiro.uitest.sweep

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.core.view.doOnNextLayout

/**
 * Created by zhangyichen on 8/29/24
 * @author zhangyichen.edden@bytedance.com
 */

/**
 * 实现扫光动画
 */
interface SweepAnimateController {
    fun startAnimate(sweepData: SweepLightView.SweepData)

    fun stopAnimate()
}


class SweepLightView: FrameLayout, SweepAnimateController {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    companion object {
        private const val MSG_START = 1
        private const val MSG_STOP = 2
    }

    private var sweepLightAnimator: ValueAnimator? = null

    private var handler = Handler(Looper.getMainLooper(), object : Handler.Callback{
        override fun handleMessage(msg: Message): Boolean {
            when(msg.what) {
                MSG_START -> {
                    (msg.obj as? SweepData)?.let {
                        startAnimateInternal(it)
                    }
                }
                MSG_STOP -> {
                    stopAnimateInternal()
                }
            }
            return true
        }
    })

    override fun startAnimate(sweepData: SweepData) {
        if (isAttachedToWindow) {
            handler.sendMessage(Message().apply {
                what = MSG_START
                obj = sweepData
            })
        }
    }

    override fun stopAnimate() {
        handler.sendMessage(Message().apply {
            what = MSG_STOP
        })
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        // 初始化扫光矩阵
        doOnNextLayout {
            val paint = Paint().apply {
                this.shader = LinearGradient(
                    0f, 0f, width.toFloat(), 0f,
                    intArrayOf(
                        Color.parseColor("#00FFFFFF"), Color.parseColor("#00FFFFFF"),
                        Color.parseColor("#1AFFFFFF"), Color.parseColor("#00FFFFFF"),
                        Color.parseColor("#00FFFFFF"),
                    ),
                    floatArrayOf(0f, 0.29f, 0.50f, 0.70f, 1f),
                    Shader.TileMode.CLAMP,
                )
            }
            background = object : Drawable() {
                override fun draw(canvas: Canvas) = canvas.drawRect(bounds, paint)
                override fun getOpacity(): Int = PixelFormat.OPAQUE
                override fun setAlpha(alpha: Int) {
                    paint.alpha = alpha
                }

                override fun setColorFilter(colorFilter: ColorFilter?) {
                    paint.colorFilter = colorFilter
                }
            }
        }
    }

    /**
     * 扫光动画原理如下：
     *         startX                       endX
     *           |                           |
     *  ----------------------------------------
     *  |扫光区域| ->
     *  ----------------------------------------
     *  控制扫光的translationX向前移动
     */
    private fun startAnimateInternal(sweepData: SweepData) {
        val sweepStartX = sweepData.startX - width.toFloat()
        val sweepEndX = sweepData.endX + width.toFloat()
        Log.d("zyc", "sweepStartX: $sweepStartX, sweepEndX: $sweepEndX")
        sweepLightAnimator = ValueAnimator.ofFloat(
            // 起点是：起点X - 扫光View的宽度
            sweepStartX,
            // 终点是：起点X
            sweepEndX
        ).also { animator ->
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // 扫光完成，直接回到初始位置
                    translationX = sweepStartX
                    animator.start()
                }
            })
            animator.addUpdateListener {
                Log.d("zyc", "animatedValue: ${it.animatedValue}")
                translationX = it.animatedValue as Float
            }
            animator.duration = sweepData.duration
            animator.start()
        }
    }

    private fun stopAnimateInternal() {
        handler.removeMessages(MSG_START)
        // 归位
        translationX = 0f
        sweepLightAnimator?.cancel()
        sweepLightAnimator?.removeAllUpdateListeners()
        sweepLightAnimator?.removeAllListeners()
        sweepLightAnimator?.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimateInternal()
    }

    data class SweepData(
        val startX: Int,
        val endX: Int,
        val duration: Long
    )
}