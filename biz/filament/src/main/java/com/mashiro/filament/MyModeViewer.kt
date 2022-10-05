package com.mashiro.filament

import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import com.google.android.filament.*
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.android.filament.utils.*

class MyModeViewer(
    val engine: Engine,
    private val uiHelper: UiHelper
) : android.view.View.OnTouchListener  {
    val scene: Scene
    val view: View
    val camera: Camera
    val renderer: Renderer

    private lateinit var displayHelper: DisplayHelper
    private lateinit var cameraManipulator: Manipulator
    private lateinit var gestureDetector: GestureDetector
    private var surfaceView: SurfaceView? = null
    private var textureView: TextureView? = null
    private var swapChain: SwapChain? = null

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
            .viewport(surfaceView.width, surfaceView.height)
            .build(Manipulator.Mode.FREE_FLIGHT)

        this.surfaceView = surfaceView
        gestureDetector = GestureDetector(surfaceView, cameraManipulator)
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
//            view.viewport = Viewport(0, 0, width, height)
//            cameraManipulator.setViewport(width, height)
//            updateCameraProjection()
            val zoom = 1.5
            val aspect = width.toDouble() / height.toDouble()
            camera.setProjection(Camera.Projection.ORTHO,
                -aspect * zoom, aspect * zoom, -zoom, zoom, 0.0, 10.0)

            view.viewport = Viewport(0, 0, width, height)
        }
    }

    private fun addDetachListener(view: android.view.View) {
        view.addOnAttachStateChangeListener(object : android.view.View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: android.view.View?) {}
            override fun onViewDetachedFromWindow(v: android.view.View?) {
                uiHelper.detach()

                engine.destroyRenderer(renderer)
                engine.destroyView(this@MyModeViewer.view)
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

        // Render the scene, unless the renderer wants to skip the frame.
        if (renderer.beginFrame(swapChain!!, frameTimeNanos)) {
            renderer.render(view)
            renderer.endFrame()
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