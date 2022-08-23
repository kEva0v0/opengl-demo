package com.mashiro.opengl

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mashiro.opengl.firstopengl.TriangleActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_opengl.setOnClickListener {
            val intent = Intent(this, TriangleActivity::class.java)
            startActivity(intent)
        }
    }
}