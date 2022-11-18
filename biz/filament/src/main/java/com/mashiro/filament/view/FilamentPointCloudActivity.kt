package com.mashiro.filament.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.filament.Skybox
import com.google.android.filament.utils.Utils
import com.mashiro.filament.MyModelViewer
import com.mashiro.filament.R
import com.mashiro.filament.model.Grid
import com.mashiro.filament.model.NormalPoint
import com.mashiro.filament.model.Point
import com.mashiro.filament.render.GridRender
import com.mashiro.filament.render.PointCloudRender
import com.mashiro.filament.utils.BitmapUtils
import kotlinx.android.synthetic.main.filament_layout.*
import java.util.*

class FilamentPointCloudActivity: AppCompatActivity() {

    companion object {
        private const val MAX_POINT_CNT = 50000
        private const val MAX_POINT_CLOUD = 20
    }

    private  var renderer : PointCloudRender? = null
    private var gridRender: GridRender? = null

    private val dataMap = mutableMapOf<Int, NormalPoint>()
    private var currentSize = 0
    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.filament_layout)
        initRender()
        initListener()
    }

    private fun initRender(){
        val surfaceView = findViewById<SurfaceView>(R.id.sv_filament)

        renderer = PointCloudRender(surfaceView)
        renderer?.loadMaterial(this)

        gridRender = GridRender(surfaceView)
        gridRender?.loadMaterial(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        val surfaceView = findViewById<SurfaceView>(R.id.sv_filament)
        sv_filament.setOnTouchListener { v, event ->
            renderer?.onTouch(event)
            gridRender?.onTouch(event)
            renderer?.requestRender()
            gridRender?.requestRender()
            true
        }
        btn_add.setOnClickListener {
            randomPoint()
            renderer?.requestRender()
            gridRender?.addFrame(Grid())
            gridRender?.requestRender()
        }
        btn_up.setOnClickListener {
            dataMap.forEach {
                it.value.moveUp()
                renderer?.move(it.value)
            }
            renderer?.requestRender()
        }
        btn_down.setOnClickListener {
            dataMap.forEach {
                it.value.moveDown()
                renderer?.move(it.value)
            }
            renderer?.requestRender()
        }
        btn_right.setOnClickListener {
            dataMap.forEach {
                it.value.moveRight()
                renderer?.move(it.value)
            }
            renderer?.requestRender()
        }
        btn_left.setOnClickListener {
            dataMap.forEach {
                it.value.moveLeft()
                renderer?.move(it.value)
            }
            renderer?.requestRender()
        }
        btn_screenshot.setOnClickListener {
            BitmapUtils.screenShot(surfaceView)
        }
    }

    override fun onResume() {
        super.onResume()
        renderer?.resume()
        renderer?.requestRender()
        gridRender?.resume()
        gridRender?.requestRender()
    }

    override fun onPause() {
        super.onPause()
        renderer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        renderer?.destroy()
    }

    private fun randomPoint(){
        currentSize += 1
        dataMap[currentSize] = createData()
        renderer?.addFrame(dataMap[currentSize]!!)
    }

    private fun createData(): NormalPoint {
        val pointList = mutableListOf<Point>()
        for (i in 0 until MAX_POINT_CNT) {
            pointList.add(
                Point(
                    random.nextFloat() * 2f - 1f,
                    random.nextFloat() * 2f - 1f,
                    0f,
                    random.nextInt(255),
                    random.nextInt(255),
                    random.nextInt(255),
                    255,
                )
            )
        }
        return NormalPoint(pointList)
    }
}