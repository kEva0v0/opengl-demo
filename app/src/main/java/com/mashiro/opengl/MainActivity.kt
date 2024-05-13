package com.mashiro.opengl

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mashiro.filament.view.FilamentActivity
import com.mashiro.filament.view.FilamentPointCloudActivity
import com.mashiro.flowtest.ColorActivity
import com.mashiro.flowtest.FlowActivity
import com.mashiro.opengl.firstopengl.TriangleActivity
import com.mashiro.uitest.UIActivity
import com.mashiro.unity.UnityPlayerActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_open_flowtest.setOnClickListener {
            val intent = Intent(this, FlowActivity::class.java)
            startActivity(intent)
        }
        btn_open_color.setOnClickListener {
            val intent = Intent(this, ColorActivity::class.java)
            startActivity(intent)
        }
        btn_open_uitest.setOnClickListener {
            val intent = Intent(this, UIActivity::class.java)
            startActivity(intent)
        }
        btn_opengl.setOnClickListener {
            val intent = Intent(this, TriangleActivity::class.java)
            startActivity(intent)
        }
        btn_filament.setOnClickListener{
            val intent = Intent(this, FilamentActivity::class.java)
            startActivity(intent)
        }
        btn_test_filament.setOnClickListener {
            val intent = Intent(this, FilamentPointCloudActivity::class.java)
            startActivity(intent)
        }
        btn_test_unity.setOnClickListener {
            val intent = Intent(this, UnityPlayerActivity::class.java)
            startActivity(intent)
        }
    }
}