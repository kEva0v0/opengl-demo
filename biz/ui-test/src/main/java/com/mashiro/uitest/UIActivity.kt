package com.mashiro.uitest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.mashiro.uitest.audioinput.AudioInputView
import kotlinx.android.synthetic.main.ui_test_activity.btn_open_activity
import kotlinx.android.synthetic.main.ui_test_activity.root
import kotlinx.android.synthetic.main.view_drawer.icon_audio_container

class UIActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_test_activity)
        btn_open_activity.setOnClickListener {
            startActivityForResult(Intent(this, DemoActivity::class.java), 0)
        }
//        setAudioView()
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