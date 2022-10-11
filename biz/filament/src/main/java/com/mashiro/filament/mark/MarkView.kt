package com.mashiro.filament.mark

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.customview.widget.ViewDragHelper
import com.mashiro.filament.R
import kotlinx.android.synthetic.main.layout_mark.view.*
import kotlin.math.cos
import kotlin.math.sin

class MarkView: ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        LayoutInflater.from(context).inflate(R.layout.layout_mark, this, true)
    }

    private var prevX = 0f
    private var prevY = 0f
    private var listener: IMarkStateListener? = null

    fun addListener(listener: IMarkStateListener){
        this.listener = listener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val radians = Math.toRadians(rotation.toDouble()).toFloat()
        val refinedTranslationX = translationX * cos(radians) + translationY * sin(radians)
        val refinedTranslationY = -translationX * sin(radians) + translationY * cos(radians)
        event.offsetLocation(refinedTranslationX, refinedTranslationY)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                prevX = event.x
                prevY = event.y
            }
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {

                val isFinalAction = event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL
                val dx = event.x - prevX
                val dy = event.y - prevY
                translate(dx, dy, isFinalAction)
                prevX = event.x
                prevY = event.y
            }
        }
        listener?.onTouchEvent(event)
        return true
    }

    private fun translate(dx: Float, dy: Float, isFinalAction: Boolean) {

        val radians = Math.toRadians(rotation.toDouble()).toFloat()
        val refinedDx = dx * cos(radians) - dy * sin(radians)
        val refinedDy = dx * sin(radians) + dy * cos(radians)
        translationX += refinedDx
        translationY += refinedDy
//        translationX += dx
//        translationY += dy
    }
}