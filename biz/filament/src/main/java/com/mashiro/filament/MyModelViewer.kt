package com.mashiro.filament

import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import com.google.android.filament.*
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.google.android.filament.utils.*

class MyModelViewer(
    val engine: Engine,
    private val uiHelper: UiHelper
) : android.view.View.OnTouchListener  {
    val scene: Scene
    val view: View
    val camera: Camera
    val renderer: Renderer
    var cameraFocalLength = 10f
        set(value) {
            field = value
            updateCameraProjection()
        }

    private  val kNearPlane = 0.05     // 5 cm
    private  val kFarPlane = 1000.0    // 1 km
    private lateinit var displayHelper: DisplayHelper
    private lateinit var cameraManipulator: Manipulator
    private lateinit var gestureDetector: MyGestureDetector
    private var surfaceView: SurfaceView? = null
    private var textureView: TextureView? = null
    private var swapChain: SwapChain? = null
    private val eyePos = DoubleArray(3)
    private val target = DoubleArray(3)
    private val upward = DoubleArray(3)

    init {
        renderer = engine.createRenderer()
        scene = engine.createScene()
        camera = engine.createCamera(engine.entityManager.create())
        view = engine.createView()
        view.scene = scene
        view.camera = camera

    }

    constructor(
        surfaceView: SurfaceView,
        engine: Engine = Engine.create(),
        uiHelper: UiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK),
        manipulator: Manipulator? = null
    ) : this(engine, uiHelper) {
        cameraManipulator = manipulator ?: Manipulator.Builder()
            .targetPosition(0f,0f,0f)
            .upVector(0f,0f,-1f)
            .orbitHomePosition(0f,10f,0f)
            .viewport(surfaceView.width, surfaceView.height)
            .orbitSpeed(0.01f,0.01f)
            .zoomSpeed(0.1f)
            .build(Manipulator.Mode.ORBIT)

        this.surfaceView = surfaceView
        gestureDetector = MyGestureDetector(surfaceView, cameraManipulator)
        displayHelper = DisplayHelper(surfaceView.context)
        uiHelper.renderCallback = SurfaceCallback()
        uiHelper.attachTo(surfaceView)
        addDetachListener(surfaceView)
    }

    inner class SurfaceCallback : UiHelper.RendererCallback {
        override fun onNativeWindowChanged(surface: Surface) {
            swapChain?.let { engine.destroySwapChain(it) }
            swapChain = engine.createSwapChain(surface)
            surfaceView?.let { displayHelper.attach(renderer, it.display) }
            textureView?.let { displayHelper.attach(renderer, it.display) }
        }

        override fun onDetachedFromSurface() {
            displayHelper.detach()
            swapChain?.let {
                engine.destroySwapChain(it)
                engine.flushAndWait()
                swapChain = null
            }
        }

        override fun onResized(width: Int, height: Int) {
            view.viewport = Viewport(0, 0, width, height)
            cameraManipulator.setViewport(width, height)
            updateCameraProjection()
//            val zoom = 1.5
//            val aspect = width.toDouble() / height.toDouble()
//            camera.setProjection(Camera.Projection.ORTHO,
//                -aspect * zoom, aspect * zoom, -zoom, zoom, 0.0, 10.0)
//
//            view.viewport = Viewport(0, 0, width, height)
        }
    }

    private fun updateCameraProjection() {
        val width = view.viewport.width
        val height = view.viewport.height
        val aspect = width.toDouble() / height.toDouble()
        camera.setLensProjection(cameraFocalLength.toDouble(), aspect, kNearPlane, kFarPlane)
    }

    private fun addDetachListener(view: android.view.View) {
        view.addOnAttachStateChangeListener(object : android.view.View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: android.view.View) {}
            override fun onViewDetachedFromWindow(v: android.view.View) {
                uiHelper.detach()

                engine.destroyRenderer(renderer)
                engine.destroyView(this@MyModelViewer.view)
                engine.destroyScene(scene)
                engine.destroyCameraComponent(camera.entity)
                EntityManager.get().destroy(camera.entity)

                engine.destroy()
            }
        })
    }

    fun render(frameTimeNanos: Long) {
        if (!uiHelper.isReadyToRender) {
            return
        }
        val start = System.nanoTime()
        cameraManipulator.getLookAt(eyePos, target, upward)
        camera.lookAt(
            eyePos[0], eyePos[1], eyePos[2],
            target[0], target[1], target[2],
            upward[0], upward[1], upward[2])
        Log.i("zyc", "eyePos: ${eyePos[0]},${eyePos[1]},${eyePos[2]}")
        Log.i("zyc", "target: ${target[0]},${target[1]},${target[2]}")
        Log.i("zyc", "upward: ${upward[0]},${upward[1]},${upward[2]}")
        // Render the scene, unless the renderer wants to skip the frame.
        if (renderer.beginFrame(swapChain!!, frameTimeNanos)) {
            renderer.render(view)
            renderer.endFrame()
            val duration = (System.nanoTime() - start) / 1000000.0
            Log.i("modelViewer-onDraw", "onDrawFrame() duration cost $duration ms")
        }
    }

    fun onTouchEvent(event: MotionEvent) {
        gestureDetector.onTouchEvent(event)
    }

    override fun onTouch(v: android.view.View, event: MotionEvent): Boolean {
        onTouchEvent(event)
        return true
    }

}