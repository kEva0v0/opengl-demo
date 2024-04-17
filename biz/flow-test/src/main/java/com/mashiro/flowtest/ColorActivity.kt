package com.mashiro.flowtest

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_color_test.btn_animate
import kotlinx.android.synthetic.main.layout_color_test.btn_clear
import kotlinx.android.synthetic.main.layout_color_test.btn_color_1
import kotlinx.android.synthetic.main.layout_color_test.btn_color_2
import kotlinx.android.synthetic.main.layout_color_test.edt_color_1
import kotlinx.android.synthetic.main.layout_color_test.edt_color_2
import kotlinx.android.synthetic.main.layout_color_test.ll_color_region
import kotlinx.android.synthetic.main.layout_color_test.sk_bar

class ColorActivity: AppCompatActivity() {

    private var colorView1 : ImageView? = null
    private var colorView2: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_color_test)
        btn_color_1.setOnClickListener {
            edt_color_1.text.toString().takeIf { it.isNotEmpty() }?.let {
                colorView1 = getColorView(it)
                ll_color_region.addView(colorView1)
            }
        }
        btn_color_2.setOnClickListener {
            edt_color_2.text.toString().takeIf { it.isNotEmpty() }?.let {
                colorView2 = getColorView(it)
                ll_color_region.addView(colorView2)
            }
        }
        btn_clear.setOnClickListener {
            ll_color_region.removeAllViews()
        }
        btn_animate.setOnClickListener {
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 2000L
                addUpdateListener {
                    colorView1?.alpha = it.animatedValue as Float
                    colorView2?.alpha = 1f - it.animatedValue as Float
                }
                start()
            }
        }
        sk_bar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener{
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val animatedValue = progress / 100f
                colorView1?.alpha = animatedValue
                colorView2?.alpha = 1f - animatedValue
            }
        })
    }

    private fun getColorView(colorStr: String): ImageView {
        val color = Color.parseColor("#$colorStr")
        return ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            setBackgroundColor(color)
        }
    }
}