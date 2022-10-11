package com.mashiro.filament.view

import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.mashiro.filament.R
import com.mashiro.filament.utils.RandomColorUtils
import com.mashiro.filament.bean.NormalPoint
import com.mashiro.filament.bean.Vertex
import com.mashiro.filament.mark.IMarkStateListener
import com.mashiro.filament.render.PointCloudRender
import com.mashiro.filament.utils.BitmapUtils
import kotlinx.android.synthetic.main.filament_layout.*
import java.util.*

class FilamentPointCloudActivity: AppCompatActivity() {

    companion object {
        private const val MAX_POINT_CNT = 50000
        private const val MAX_POINT_CLOUD = 20
    }

    private lateinit var renderer : PointCloudRender
    private val dataMap = mutableMapOf<Int, NormalPoint>()
    private var currentSize = 0
    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.filament_layout)
        val surfaceView = findViewById<SurfaceView>(R.id.sv_filament)

        renderer = PointCloudRender(surfaceView)
        renderer.loadMaterial(this)
        dataMap[currentSize] = createData()
        renderer.addFrame(dataMap[currentSize]!!)
        sv_filament.setOnTouchListener { v, event ->
            renderer.onTouch(event)
            renderer.requestRender()
            true
        }
        btn_add.setOnClickListener {
            randomPoint()
            renderer.requestRender()
        }
        btn_add_all.setOnClickListener {
            if (currentSize < MAX_POINT_CLOUD){
                for (i in currentSize until MAX_POINT_CLOUD){
                    currentSize += 1
                    dataMap[currentSize] = createData()
                    renderer.addFrame(dataMap[currentSize]!!)
                }
            }
            renderer.requestRender()
        }
        btn_up.setOnClickListener {
            dataMap.forEach {
                it.value.moveUp()
                renderer.move(it.value)
            }
            renderer.requestRender()
        }
        btn_down.setOnClickListener {
            dataMap.forEach {
                it.value.moveDown()
                renderer.move(it.value)
            }
            renderer.requestRender()
        }
        btn_right.setOnClickListener {
            dataMap.forEach {
                it.value.moveRight()
                renderer.move(it.value)
            }
            renderer.requestRender()
        }

        btn_left.setOnClickListener {
            dataMap.forEach {
                it.value.moveLeft()
                renderer.move(it.value)
            }
            renderer.requestRender()
        }
        btn_screenshot.setOnClickListener {
            BitmapUtils.screenShot(surfaceView)
        }

        mv_mark.addListener(object : IMarkStateListener{
            override fun onTouchEvent(event: MotionEvent) {
                renderer.onTouch(event)
                renderer.requestRender()
            }

            override fun onSelected() {

            }
        })
    }


    private fun createData(): NormalPoint{
        val pointList = mutableListOf<Vertex>()
        for (i in 0 until MAX_POINT_CNT){
            pointList.add(Vertex(random.nextFloat()*1f-0.5f, random.nextFloat()*1f-0.5f, 0f, RandomColorUtils.getRandomColor()))
        }
        return NormalPoint(pointList)
    }

    override fun onResume() {
        super.onResume()
        renderer.resume()
//        renderer.setFrame(normalPoint)
        renderer.requestRender()
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
        currentSize += 1
        dataMap[currentSize] = createData()
        renderer.addFrame(dataMap[currentSize]!!)
    }
}