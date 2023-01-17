package com.mashiro.renderengine.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.mashiro.renderengine.Constants
import com.mashiro.renderengine.engine.FilamentEngine
import com.mashiro.renderengine.engine.impl.EngineConfig
import com.mashiro.renderengine.math.Position
import com.mashiro.renderengine.math.times
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float4
import dev.romainguy.kotlin.math.Mat4

open class FilamentView: SurfaceView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val choreographer = Choreographer.getInstance()
    private val frameCallback = FrameCallback()
    var filamentEngine : FilamentEngine? = null
    private var engineConfig: EngineConfig = EngineConfig()

    fun setLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : DefaultLifecycleObserver{
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                choreographer.postFrameCallback(frameCallback)
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                choreographer.removeFrameCallback(frameCallback)
            }
        })
    }

    // 在 viewCreated的时候最好调用一次
    fun initEngine(engineConfig: EngineConfig = EngineConfig()) {
        this.engineConfig = engineConfig
        filamentEngine = FilamentEngine(this, engineConfig = engineConfig)
    }

    inner class FrameCallback : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            // Schedule the next frame
            choreographer.postFrameCallback(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(Constants.TAG, "onAttachedToWindow create Engine")
        if (filamentEngine == null) {
            filamentEngine = FilamentEngine(this, engineConfig = this.engineConfig)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(Constants.TAG, "onAttachedToWindow destroy Engine")
        filamentEngine?.destroy()
        filamentEngine = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        filamentEngine?.onTouch(this, event)
        filamentEngine?.updateCameraLookAt()
        filamentEngine?.invokeListeners(event)
        filamentEngine?.drawFrame()
        return true
    }
}