package com.mashiro.uitest.audioinput

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Interpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.mashiro.uitest.R
import com.mashiro.uitest.utils.UIUtils
import kotlinx.android.synthetic.main.view_drawer.view.demo_input_view
import kotlinx.android.synthetic.main.view_drawer.view.icon_audio_container
import kotlinx.android.synthetic.main.view_drawer.view.iv_listening
import kotlinx.android.synthetic.main.view_drawer.view.iv_prepare_to_listen
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sqrt

class AudioInputView: ConstraintLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    // region drawable区域
    private val drawable = GradientDrawable()
    // endregion

    // region animate区域
    private var iconRotateAnimator: ValueAnimator? = null
    private var inputSpringAnimation: SpringAnimation? = null
    // endregion

    // region 状态变量
    private var inputModel: InputModel = InputModel.NORMAL
    private var startTime = 0L
    // endregion

    private fun init(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.view_drawer, this, true)
        drawable.cornerRadius = UIUtils.dp2pxFloat(context, 16f)
        drawable.setColor(Color.argb(100,0,0,0))
        demo_input_view.background = drawable
    }

    fun setAudioListener() {
        icon_audio_container.setOnClickListener {
            if (inputModel == InputModel.AUDIO) {
                inputModel = InputModel.NORMAL
                updateInputModel(InputModel.AUDIO, InputModel.NORMAL)
            } else {
                inputModel = InputModel.AUDIO
                updateInputModel(InputModel.NORMAL, InputModel.AUDIO)
            }
        }
    }

    private fun updateInputModel(preModel: InputModel, curModel: InputModel, withAnimate: Boolean = true) {
        if (preModel == curModel) {
            return
        }
        iconRotateAnimator?.cancel()
        iconRotateAnimator = null
        iconRotateAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            addUpdateListener {
                if (this@AudioInputView.isAttachedToWindow) {
                   icon_audio_container.rotation = 135f * (it.animatedValue as Float)
                }
                iv_prepare_to_listen.alpha = 1f - it.animatedValue as Float
                iv_listening.alpha = it.animatedValue as Float
            }
        }
        inputSpringAnimation?.cancel()
        inputSpringAnimation = null
        val initialWidth = UIUtils.dp2px(context, 54f)
        val fullyWidth = UIUtils.getAppScreenWidth(context) - UIUtils.dp2px(context, 20f)
        val targetWidth = if (curModel == InputModel.AUDIO) {
            fullyWidth
        } else {
            initialWidth
        }
        val customProperty = object : FloatPropertyCompat<View>("width") {
            override fun getValue(view: View): Float {
                // 获取自定义属性的当前值
                return 0f
            }

            override fun setValue(view: View, value: Float) {
//                view.updateLayoutParams<ConstraintLayout.LayoutParams> {
//                    width = value.toInt()
//                }
            }
        }

        inputSpringAnimation = SpringAnimation(demo_input_view, customProperty)
        inputSpringAnimation?.addUpdateListener{ _, animateValue, _ ->
            val curTime = System.currentTimeMillis()
            Log.d("zyc", "duration:${curTime - startTime}, value:$animateValue")
            startTime = curTime
        }
        val springForce = SpringForce()
        springForce.finalPosition = 1f // 设置自定义属性的目标值
        springForce.stiffness = 711.1f
        val mass = 1f
        springForce.dampingRatio = 40 / (2f * sqrt(springForce.stiffness * mass))
        inputSpringAnimation?.spring = springForce
        when{
            preModel == InputModel.NORMAL && curModel == InputModel.AUDIO -> {
                iconRotateAnimator?.start()
                startTime = System.currentTimeMillis()
                inputSpringAnimation?.start()
            }
            preModel == InputModel.AUDIO && curModel == InputModel.NORMAL -> {
                iconRotateAnimator?.reverse()
                startTime = System.currentTimeMillis()
                inputSpringAnimation?.start()
            }
        }
    }


    // region 数据定义
    enum class InputModel{
        NORMAL,
        AUDIO
    }

    // pow(2, -10 * x) * sin((x - factor / 4) * (2 * PI) / factor) + 1
//    class StiffnessInterceptor(
//        factor: Float
//    ): Interpolator {
//        override fun getInterpolation(input: Float): Float {
//            val frequency = sqrt(stiffness.toDouble() / mass).toFloat()
//            val decay = damping / (2 * mass)
//            val omega = frequency * sqrt(1 - decay * decay)
//            val x = input - 1f
//            return -exp(-decay * x).toFloat() * cos(omega * x)
//        }
//    }

    // endregion
}