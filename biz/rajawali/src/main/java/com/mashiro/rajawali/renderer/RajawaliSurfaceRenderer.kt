package com.mashiro.rajawali.renderer

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.MotionEvent
import org.rajawali3d.Object3D
import org.rajawali3d.renderer.ISurfaceRenderer
import org.rajawali3d.renderer.Renderer
import org.rajawali3d.view.ISurface
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class RajawaliSurfaceRenderer(context: Context) : Renderer(context) {

    override fun onRenderSurfaceCreated(config: EGLConfig?, gl: GL10?, width: Int, height: Int) {
        super.onRenderSurfaceCreated(config, gl, width, height)
    }

    override fun onRenderSurfaceSizeChanged(gl: GL10?, width: Int, height: Int) {
        super.onRenderSurfaceSizeChanged(gl, width, height)
    }

    override fun onOffsetsChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {

    }

    override fun onTouchEvent(event: MotionEvent?) {

    }

    override fun initScene() {
    }

    override fun onRenderFrame(gl: GL10?) {
        super.onRenderFrame(gl)
    }
}