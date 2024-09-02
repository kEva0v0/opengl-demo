package com.mashiro.uitest

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.mashiro.uitest.audioinput.AudioInputView
import kotlinx.android.synthetic.main.ui_test_activity.btn_close_sweep
import kotlinx.android.synthetic.main.ui_test_activity.btn_open_activity
import kotlinx.android.synthetic.main.ui_test_activity.btn_test_sweep
import kotlinx.android.synthetic.main.ui_test_activity.root
import kotlinx.android.synthetic.main.ui_test_activity.tips_item_view
import kotlinx.android.synthetic.main.ui_test_activity.tv_hello
import kotlinx.android.synthetic.main.view_drawer.icon_audio_container

class UIActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_test_activity)
        btn_open_activity.setOnClickListener {
            startActivityForResult(Intent(this, DemoActivity::class.java), 0)
        }
//        setAudioView()
        val originText = "hello world,hello bro, how are you today? fine thank you"
        val spannableString = SpannableString(originText)
        spannableString.setSpan(ForegroundColorSpan(Color.RED), 0, 20, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        TextUtils.substring(originText, 0, 3)
        tv_hello.text = spannableString

        btn_test_sweep.setOnClickListener {
            tips_item_view.showSweepAnimate()
        }
        btn_close_sweep.setOnClickListener {
            tips_item_view.hideSweepAnimate()
        }
    }

    // 测试一下输入框抽屉特效
    private fun setAudioView() {
        val view = AudioInputView(this)
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        view.layoutParams = layoutParams
        view.setAudioListener()
        root.addView(view)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("zyc", "onActivityResult UIActivity")
    }
}