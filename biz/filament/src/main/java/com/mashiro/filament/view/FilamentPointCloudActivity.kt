package com.mashiro.filament.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
import com.mashiro.filament.utils.FileUtils
import com.mashiro.filament.viewmodel.PointCloudViewModel
import kotlinx.android.synthetic.main.filament_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.util.*
import kotlin.math.abs

class FilamentPointCloudActivity: AppCompatActivity() {

    companion object {
        private const val MAX_POINT_CNT = 100000
        private const val MAX_POINT_CLOUD = 20

        private const val DEBUG_ALGORITHM_FOLDER_NAME = "debug_algorithm"
        private const val POINT_CLOUD_PERFORMANCE_FOLDER_NAME = "point_cloud"
        private const val POINT_CLOUD_FILE_NAME = "point_cloud.ply"
    }

    private val pointCloudViewModel : PointCloudViewModel by viewModels()
    private  var renderer : PointCloudRender? = null
    private var gridRender: GridRender? = null
    private lateinit var modelViewer: MyModelViewer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.filament_layout)
        initRender()
        initListener()
        initViewModel()
    }

    private fun initRender(){
        val surfaceView = findViewById<SurfaceView>(R.id.sv_filament)
        Utils.init()
        modelViewer = MyModelViewer(surfaceView)
        modelViewer.scene.skybox = Skybox.Builder().color(0.035f, 0.035f, 0.035f, 1.0f).build(modelViewer.engine)

        renderer = PointCloudRender(surfaceView, modelViewer)
        gridRender = GridRender(surfaceView, modelViewer)
        renderer?.loadMaterial(this)
        gridRender?.loadMaterial(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        sv_filament.setOnTouchListener { v, event ->
            renderer?.onTouch(event)
            gridRender?.onTouch(event)
            renderer?.requestRender()
            modelViewer.view.viewport.let {
                Log.d("zyc", "l:${it.left} b:${it.bottom} r:${it.left+it.width-1} t:${it.width+it.height-1}")
            }
            true
        }
        btn_add.setOnClickListener {
            lifecycleScope.launch{
                val normalPoint = withContext(Dispatchers.IO){
                    val pt = NormalPoint.createFromInputStream(FileInputStream(getPlyFilePath()))
                    val minZ = pt.minZ()
                    pt.pointList.forEach {
                        it.z = it.z + abs(minZ) + 0.001f
                    }
                    pt
                }
//                for (i in 0..50){
//                    val emptyList = mutableListOf<Point>()
//                    normalPoint.pointList.forEach {
//                        val offsetX = Random().nextFloat() * 0.1f
//                        val offsetZ = Random().nextFloat() * 0.1f
//                        emptyList.add(it.copy(it.x + offsetX, it.y, it.z + offsetZ))
//                    }
//                    val newPoint = NormalPoint(emptyList)
                    renderer?.addFrame(normalPoint)
                    gridRender?.addFrame(Grid())
                    renderer?.requestRender()
//                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sv_filament.post {
//            gridRender?.addFrame(Grid())
//            gridRender?.requestRender()
        }
    }

    override fun onPause() {
        super.onPause()
        renderer?.pause()
    }
    override fun onDestroy() {
        super.onDestroy()
        renderer?.destroy()
    }

    private fun initViewModel(){
        lifecycleScope.launchWhenCreated {
            withContext(Dispatchers.IO) {
                copyAssetsToFiles()
            }
        }
    }

    private fun copyAssetsToFiles() {
        FileUtils.saveInputStream(
            resources.assets.open("$POINT_CLOUD_PERFORMANCE_FOLDER_NAME/$POINT_CLOUD_FILE_NAME"),
            getOutputFolderPath(),
            POINT_CLOUD_FILE_NAME
        )
    }

    private fun getOutputFolderPath() = filesDir.absolutePath + "/$DEBUG_ALGORITHM_FOLDER_NAME/$POINT_CLOUD_PERFORMANCE_FOLDER_NAME"

    private fun getPlyFilePath(): String = "${getOutputFolderPath()}/$POINT_CLOUD_FILE_NAME"
}