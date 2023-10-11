package com.mashiro.uitest.fragment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TestFlowViewModel: ViewModel() {

    private val demoChannel = Channel<String>(100)

    private var demoJob : Job? = null

    private val staredFlow = MutableSharedFlow<String>(1)

    private var startValue = 0

    fun start() {
        viewModelScope.launch {
            while (true) {
                if (demoJob != null) {
                    Log.d("zyc", "$demoJob")
                    demoJob?.join()
                } else {
                    val what = demoChannel.receive()
                    Log.d("zyc", "collectLatest: $what")
                }
            }
        }
    }

    fun emit() {
        viewModelScope.launch {
            emitInternal("${startValue++}", false)
        }
    }

    fun emitWithAnim() {
        viewModelScope.launch {
            emitInternal("${startValue++}", true)
        }
    }

    private suspend fun emitInternal(msg: String, withAnim: Boolean) {
        if (withAnim) {
            demoJob = viewModelScope.launch {
                demoChannel.send(msg)
                delay(10000L)
                demoJob?.cancel()
                demoJob = null
            }
            demoJob?.start()
        } else {
            demoChannel.send(msg)
        }
    }
}