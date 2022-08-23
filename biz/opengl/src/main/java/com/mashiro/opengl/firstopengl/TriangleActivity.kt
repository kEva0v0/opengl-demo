package com.mashiro.opengl.firstopengl

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mashiro.opengl.firstopengl.api.GLThreadRunner
import com.mashiro.opengl.firstopengl.normal.viewmodel.TriangleViewModel
import com.mashiro.opengl.firstopengl.vbo.viewmodel.VBOViewModel
import kotlinx.android.synthetic.main.firstopengl_triangle.*

class TriangleActivity: AppCompatActivity() {


    private val viewModel: TriangleViewModel by viewModels()
    private val vboViewModel: VBOViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.firstopengl_triangle)
        initPointCloudView()
        initClickListener()
    }

    override fun onResume() {
        super.onResume()
        psv_test.onResume()
    }

    override fun onPause() {
        super.onPause()
        psv_test.onPause()
    }

    private fun initPointCloudView(){
        psv_test.init(vboViewModel)
    }

    private fun initClickListener(){
        btn_add_item.setOnClickListener {
            vboViewModel.addPoint()
            psv_test.requestRender()
        }
        btn_delete_item.setOnClickListener {
            vboViewModel.clearAll()
            psv_test.requestRender()
        }
        btn_remove_item.setOnClickListener {
            vboViewModel.removeLastPoint()
            psv_test.requestRender()
        }
        btn_left.setOnClickListener {
            vboViewModel.normalPoint.moveLeft()
            psv_test.requestRender()
        }
        btn_right.setOnClickListener {
            vboViewModel.normalPoint.moveRight()
            psv_test.requestRender()
        }
        btn_up.setOnClickListener {
            vboViewModel.normalPoint.moveUp()
            psv_test.requestRender()
        }
        btn_down.setOnClickListener {
            vboViewModel.normalPoint.moveDown()
            psv_test.requestRender()
        }
    }
}