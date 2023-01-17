package com.mashiro.renderengine.engine

import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import com.mashiro.renderengine.Constants
import com.mashiro.renderengine.engine.api.GesturePipeline
import com.mashiro.renderengine.engine.api.RenderNodePipeline
import com.mashiro.renderengine.engine.api.ViewPipeline
import com.mashiro.renderengine.engine.impl.EngineConfig
import com.mashiro.renderengine.engine.impl.GesturePipelineImpl
import com.mashiro.renderengine.engine.impl.RenderNodePipelineImpl
import com.mashiro.renderengine.math.Entity
import com.mashiro.renderengine.math.Position
import com.mashiro.renderengine.model.PointCloud
import com.mashiro.renderengine.utils.*
import com.google.android.filament.*
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper

/**
 * 负责：
 * - 初始化
 * - 管理Node
 * - 管理手势
 * - 渲染
 * - 销毁
 *
 * 屏幕坐标：filament使用的是右手坐标系
 *
 * ------- +y ----- -z
 *
 * ---------|----/----
 *
 * ---------|--/------
 *
 * -x - - - 0 - - - +x
 *
 * ------/--|---------
 *
 * ----/----|---------
 *
 * +z ---- -y --------
 */
class FilamentEngine(
    private val surfaceView: SurfaceView,
    private val engine: Engine = Engine.create(),
    private val uiHelper: UiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK),
    override val engineConfig: EngineConfig = EngineConfig(),
    private val scene: Scene = engine.createScene(),
    override val view: View = engine.createView(),
    override val camera: Camera = engine.createCamera(engine.entityManager.create()),
    private val renderer: Renderer = engine.createRenderer()
): RenderNodePipeline by RenderNodePipelineImpl(engine, scene, engineConfig),
    GesturePipeline by GesturePipelineImpl(surfaceView, engineConfig, camera, view),
    ViewPipeline
{

    var cameraFocalLength = engineConfig.cameraFocalLength
        set(value) {
            field = value
            updateCameraProjection()
        }

    private var displayHelper: DisplayHelper

    private var textureView: TextureView? = null
    private var swapChain: SwapChain? = null

    init {
        view.scene = scene
        view.camera = camera
        scene.skybox = Skybox.Builder().color(engineConfig.backGroundColor.r, engineConfig.backGroundColor.g, engineConfig.backGroundColor.b, engineConfig.backGroundColor.a).build(engine)
        displayHelper = DisplayHelper(surfaceView.context)
        uiHelper.renderCallback = SurfaceCallback()
        uiHelper.attachTo(surfaceView)
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
        }
    }

    fun drawFrame() {
        Log.d(Constants.TAG_STEP, "engine rebuild()")
        rebuild()
        // 渲染
        Log.d(Constants.TAG_STEP, "engine drawFrame")
        val sysTime = System.nanoTime()
        if (!uiHelper.isReadyToRender) {
            return
        }
        cameraManipulator.getLookAt().let { (eye, target, upward) ->
            camera.lookAt(eye, target, upward)
        }
        // Render the scene, unless the renderer wants to skip the frame.
        if (renderer.beginFrame(swapChain!!, sysTime)) {
            renderer.render(view)
            renderer.endFrame()
        }
        Log.d(Constants.TAG_STEP, "engine finishBuild()")
        finishBuild()
    }

    fun destroy() {
        // 销毁手势listener
        releaseGesture()
        // 先把Node全销毁了
        destroyNodes()
        // 销毁引擎
        uiHelper.detach()

        engine.destroyRenderer(renderer)
        engine.destroyView(this@FilamentEngine.view)
        engine.destroyScene(scene)
        engine.destroyCameraComponent(camera.entity)
        EntityManager.get().destroy(camera.entity)

        engine.destroy()
    }

    /**
     * ViewPipeline实现
     */
    override fun moveNodeWithNativeView(entity: Entity, distance: Position) {
        // 这里是下正上负的z轴
        val dis = distanceFromViewportToWorld(distance)
        updateNode(entity, false){
            it.addTransform(position = dis)
        }
        drawFrame()
    }

    override fun adjustNativeViewWithCentroid(entity: Entity, view: android.view.View?) {
        if (view != null){
            getNode(entity)?.let{ nd ->
                val worldCentroid = nd.getWorldCentroid() ?: Position()
                val worldOffset = worldToViewport(worldCentroid)
                Log.d("test-view", "worldOffset is :$worldOffset")
                view.let {
                    it.x = (worldOffset.x) - (1.0f * it.width / 2f)
                    it.y = (worldOffset.y) - (1.0f * it.height / 2f)
                }
            }
        }
    }
}