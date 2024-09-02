package com.mashiro.uitest.sweep

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.doOnNextLayout
import com.mashiro.uitest.databinding.GameCommonTipsItemViewBinding

/**
 * Created by zhangyichen on 9/2/24
 * @author zhangyichen.edden@bytedance.com
 */
class TipsItemView: FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    private val binding: GameCommonTipsItemViewBinding = GameCommonTipsItemViewBinding.inflate(
        LayoutInflater.from(context), this)

    fun showSweepAnimate() {
        binding.sweepLightView.visibility = View.VISIBLE
        binding.sweepLightView.startAnimate(
            SweepLightView.SweepData(
                startX = 0,
                endX = width,
                duration = 800
            )
        )
}

    fun hideSweepAnimate() {
        binding.sweepLightView.stopAnimate()
        binding.sweepLightView.visibility = View.GONE
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.sweepLightView.stopAnimate()
    }
}