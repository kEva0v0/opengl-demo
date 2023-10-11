package com.mashiro.uitest.fragment

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.mashiro.uitest.R
import com.mashiro.uitest.bean.SerializeTest
import com.mashiro.uitest.testspan.LoadingIndicatorImageSpan
import kotlinx.android.synthetic.main.fragment_loading_imagespan.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class LoadingImageSpanFragment: Fragment() {

    protected lateinit var rootView: View

    private val viewModel by activityViewModels<TestUiChannelViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_loading_imagespan, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var str = "Loading..."
        tv_test_image_span.updateText(str)
        lifecycleScope.launch {
            for (item in 0..5) {
                delay(300L)
                str += "....$item"
                tv_test_image_span.updateText(str)
            }
        }
        btn_collect.setOnClickListener {
            viewModel.init()
        }
        btn_emit.setOnClickListener {
            viewModel.send()
        }
        btn_emit_withanim.setOnClickListener {
            viewModel.sendDelayed()
        }
        SerializeTest.test()
    }
}