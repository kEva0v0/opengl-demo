package com.mashiro.uitest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_demo_activity.btn_open_activity

class DemoActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_demo_activity)
        btn_open_activity.setOnClickListener {
            finish()
            startActivityForResult(Intent(this, Demo2Activity::class.java), 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("zyc", "onActivityResult:${this::class.java.simpleName} ${this.hashCode()}")
    }
}