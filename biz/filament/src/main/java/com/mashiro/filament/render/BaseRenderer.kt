package com.mashiro.filament.render

import android.app.Activity
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceView
import com.google.android.filament.*
import com.google.android.filament.utils.Manipulator
import com.google.android.filament.utils.Utils
import com.mashiro.filament.MyModelViewer

abstract class BaseRenderer<Model>(
    private val surfaceView: SurfaceView,
    private val mmodelViewer: MyModelViewer,
    model: Manipulator.Mode = Manipulator.Mode.ORBIT
) {

    init {
//        Utils.init()
//        initEngine(model)
    }

    private val choreographer = Choreographer.getInstance()
    private val frameCallback = FrameCallback()
    protected val modelViewer: MyModelViewer = mmodelViewer
    protected lateinit var material: Material

//    private fun initEngine(manipulatorModel: Manipulator.Mode) {
//        modelViewer = MyModelViewer(surfaceView)
//        modelViewer.scene.skybox = Skybox.Builder().color(0.035f, 0.035f, 0.035f, 1.0f).build(modelViewer.engine)
//    }

    abstract fun loadMaterial(activity: Activity)

    abstract fun addFrame(model: Model)

    abstract fun destroy()

    // TODO：下面的全部要移走
    fun resume(){
        choreographer.postFrameCallback(frameCallback)
    }

    fun pause(){
        choreographer.removeFrameCallback(frameCallback)
    }

    fun onTouch(event: MotionEvent) {
        modelViewer?.onTouchEvent(event)
    }

    fun requestRender(){
        modelViewer?.render(System.nanoTime())
    }

    inner class FrameCallback : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            // Schedule the next frame
            choreographer.postFrameCallback(this)
        }
    }
}