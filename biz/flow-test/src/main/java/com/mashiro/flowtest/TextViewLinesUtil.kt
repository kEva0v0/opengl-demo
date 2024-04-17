package com.mashiro.flowtest

import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.widget.TextView
import androidx.annotation.RequiresApi


object TextViewLinesUtil {

    fun calculateTextViewLines(text: CharSequence, textView: TextView, textViewWidth: Int): Int {
        return buildStaticLayout(text, textView, textViewWidth).lineCount
    }

    fun buildStaticLayout(
        text: CharSequence,
        textView: TextView,
        textViewWidth: Int
    ): StaticLayout {
        val width = textViewWidth - textView.compoundPaddingLeft - textView.compoundPaddingRight
        val staticLayout: StaticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getStaticLayout23(text, textView, width)
        } else {
            getStaticLayout(text, textView, width)
        }
        return staticLayout
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun getStaticLayout23(text: CharSequence, textView: TextView, width: Int): StaticLayout {
        val builder = StaticLayout.Builder.obtain(
            text,
            0, text.length, textView.paint, width
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.LOCALE)
            .setLineSpacing(textView.lineSpacingExtra, textView.lineSpacingMultiplier)
            .setIncludePad(textView.includeFontPadding)
            .setBreakStrategy(textView.breakStrategy)
            .setHyphenationFrequency(textView.hyphenationFrequency)
            .setMaxLines(if (textView.maxLines == -1) Int.MAX_VALUE else textView.maxLines)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setJustificationMode(textView.justificationMode)
        }
        if (textView.ellipsize != null && textView.keyListener == null) {
            builder.setEllipsize(textView.ellipsize)
                .setEllipsizedWidth(width)
        }
        return builder.build()
    }

    /**
     * sdk<23
     */
    private fun getStaticLayout(text: CharSequence, textView: TextView, width: Int): StaticLayout {
        return StaticLayout(
            text,
            0, text.length,
            textView.paint, width, Layout.Alignment.ALIGN_NORMAL,
            textView.lineSpacingMultiplier,
            textView.lineSpacingExtra, textView.includeFontPadding, textView.ellipsize,
            width
        )
    }

    /**
     * 获取文本行数
     * @param textView  控件
     * @param textViewWidth   控件的宽度  比如：全屏显示-就取手机的屏幕宽度即可。
     * @return
     */
    fun calculateTextViewLines(textView: TextView, textViewWidth: Int): Int {
        val width = textViewWidth - textView.compoundPaddingLeft - textView.compoundPaddingRight
        val staticLayout: StaticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getStaticLayout23(textView.text, textView, width)
        } else {
            getStaticLayout(textView.text, textView, width)
        }
        return staticLayout.lineCount
    }
}
