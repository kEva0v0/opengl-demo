package com.mashiro.uitest.testspan

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.mashiro.uitest.R
import kotlinx.android.synthetic.main.fragment_loading_imagespan.*

@SuppressLint("AppCompatCustomView")
class TestMyTextView: TextView {
    constructor(context: Context) : super(context){}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var span: LoadingIndicatorImageSpan = LoadingIndicatorImageSpan(context, this, R.drawable.icon_search_item)

    fun updateText(text: String) {
        val str = SpannableString("$text.")
        str.setSpan(span, str.length - 1, str.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        this.text = str
        if (!span.needAnim) {
            span.needAnim = true
            span.startAnim()
        }
    }
}