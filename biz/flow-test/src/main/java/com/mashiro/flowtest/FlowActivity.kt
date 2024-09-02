package com.mashiro.flowtest

import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mashiro.flowtest.willcollectwait.WillCollectWait
import kotlinx.android.synthetic.main.layout_flow_test.btn_shared_flow_start_collect
import kotlinx.android.synthetic.main.layout_flow_test.btn_shared_flow_stop_collect
import kotlinx.android.synthetic.main.layout_flow_test.btn_shared_flow_test
import kotlinx.android.synthetic.main.layout_flow_test.tv_output


class FlowActivity: AppCompatActivity() {

    private val flowViewModel : FlowViewModel by viewModels()

    private val willCollectWait = WillCollectWait()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_flow_test)
        // flowViewModel.collecting(this)
        willCollectWait.receiver()
        btn_shared_flow_test.setOnClickListener {
            Log.d("zyc", "isFullScreen:${isFullScreen()}")
            // flowViewModel.startEmit()
            willCollectWait.sender()
        }
        btn_shared_flow_start_collect.setOnClickListener {
            // flowViewModel.startCollect(this)
            flowViewModel.testFlow()
        }
        btn_shared_flow_stop_collect.setOnClickListener {
            // flowViewModel.stopCollect()
        }
    }

    override fun onResume() {
        super.onResume()
        btn_shared_flow_test.paint.isFakeBoldText = true
        Log.d("zyc", "layout ${TextViewLinesUtil.buildStaticLayout("abc", tv_output, 20).height}")
        Log.d("zyc", "layout ${TextViewLinesUtil.buildStaticLayout("abc", tv_output, 40).height}")
        Log.d("zyc", "layout ${TextViewLinesUtil.buildStaticLayout("abc", tv_output, 63).height}")
        Log.d("zyc", "layout ${TextViewLinesUtil.buildStaticLayout("abc", tv_output, 100).height}")
    }

    fun isFullScreen(): Boolean {
        val flg = window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN
        return flg != 0
    }
}