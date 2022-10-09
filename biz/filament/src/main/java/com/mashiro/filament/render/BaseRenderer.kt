package com.mashiro.filament.render

import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceView
import com.google.android.filament.*
import com.google.android.filament.utils.Manipulator
import com.google.android.filament.utils.Utils
import com.mashiro.filament.MyModeViewer

abstract class BaseRenderer<Model>(
    private val surfaceView: SurfaceView,
    model: Manipulator.Mode = Manipulator.Mode.ORBIT
) {

    init {
        Utils.init()
        initEngine(model)
    }

    private val choreographer = Choreographer.getInstance()
    private val frameCallback = FrameCallback()
    protected lateinit var modelViewer: MyModeViewer

    private fun initEngine(manipulatorModel: Manipulator.Mode) {
        modelViewer = MyModeViewer(surfaceView)
        modelViewer.scene.skybox = Skybox.Builder().color(0.035f, 0.035f, 0.035f, 1.0f).build(modelViewer.engine)
    }

    abstract fun addFrame(model: Model)

    abstract fun destroy()

    fun resume(){
        choreographer.postFrameCallback(frameCallback)
    }

    fun pause(){
        choreographer.removeFrameCallback(frameCallback)
    }

    fun onTouch(event: MotionEvent) {
        modelViewer.onTouchEvent(event)
    }

    fun requestRender(){
        modelViewer.render(System.nanoTime())
    }

    inner class FrameCallback : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            // Schedule the next frame
            choreographer.postFrameCallback(this)
        }
    }
}