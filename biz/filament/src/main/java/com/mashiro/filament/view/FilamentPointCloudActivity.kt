package com.mashiro.filament.view

import android.os.Bundle
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.mashiro.filament.R
import com.mashiro.filament.bean.NormalPoint
import com.mashiro.filament.bean.Point
import com.mashiro.filament.bean.Vertex
import com.mashiro.filament.render.PointCloudRender
import kotlinx.android.synthetic.main.filament_layout.*
import java.util.*

class FilamentPointCloudActivity: AppCompatActivity() {

    private lateinit var renderer : PointCloudRender
    private val normalPoint = NormalPoint(
        mutableListOf(
            Vertex(0.1f,0.1f,0f, 0xffff0000.toInt()),
            Vertex(0f, 0.1f, 0f, 0xff00ff00.toInt()),
            Vertex(0f, 0f, 0f, 0xff0000ff.toInt())
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.filament_layout)
        val surfaceView = findViewById<SurfaceView>(R.id.sv_filament)
        renderer = PointCloudRender(surfaceView)
        renderer.setFrame(normalPoint)
        surfaceView.setOnTouchListener{ _, event ->
            renderer.onTouch(event)
            true
        }

        btn_add.setOnClickListener {
            randomPoint()
        }
    }

    override fun onResume() {
        super.onResume()
        renderer.resume()
//        renderer.setFrame(normalPoint)
    }

    override fun onPause() {
        super.onPause()
        renderer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        renderer.destroy()
    }

    private fun randomPoint(){
        val random = Random()
        val normalPoint = NormalPoint(
            mutableListOf(
                Vertex(random.nextFloat()*0.2f,random.nextFloat()*0.2f,0f, 0xffff0000.toInt()),
                Vertex(random.nextFloat()*0.2f,random.nextFloat()*0.2f, 0f, 0xff00ff00.toInt()),
                Vertex(random.nextFloat()*0.2f,random.nextFloat()*0.2f, 0f, 0xff0000ff.toInt())
            )
        )
        renderer.setFrame(normalPoint)
    }
}