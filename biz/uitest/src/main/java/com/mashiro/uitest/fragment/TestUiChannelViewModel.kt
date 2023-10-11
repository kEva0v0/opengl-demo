package com.mashiro.uitest.fragment

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*

class TestUiChannelViewModel: ViewModel() {

    private val stateFlow = MutableStateFlow<String>("init")

    private val uiChannel = Channel<UiEvent>()

    private val internalChannel = Channel<Int>()

    private var timeCnt = 0
    private var cnt = 0

    private var job : CompletableJob? = null

    fun init() {
        val testDD1 = TestDD("aa")
        val testDD2 = TestDD("aa")
        Log.d("zyc", "equals ${testDD1 == testDD2}")
        viewModelScope.launch {
            stateFlow.collect{
                Log.d("zyc", "stateflow collect $it")
            }
        }
        viewModelScope.launch {
            subscribeUiEvent()
        }
    }

    private suspend fun subscribeUiEvent() {
        while (true) {
            val str = uiChannel.receive()
            Log.d("zyc", "proceed ui event $str")
            if (str.jobNeed) {
                val timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        runInMainThread {
                            Log.d("zyc", "timer run on $timeCnt")
                            if (timeCnt++ == 10) {
                                Log.d("zyc", "job reset start $job")
                                timeCnt = 0
                                viewModelScope.launch {
                                    internalChannel.send(1)
                                    Log.d("zyc", "job reset complete $job")
                                }
                                timer.cancel()
                            }
                        }
                    }}, 0, 500L)
                Log.d("zyc", "job join $job")
                internalChannel.receive()
                Log.d("zyc", "job join complete $job")
            }
            Log.d("zyc", "receive ui event $str")
        }
    }

    fun sendDelayed() {
        viewModelScope.launch {
            stateFlow.emit("ohhhh!!send delayed")
            uiChannel.send(UiEvent("send delayed event ${cnt++}", true))
        }
    }

    fun send() {
        viewModelScope.launch {
            stateFlow.emit("ohhhh!!send!")
            uiChannel.send(UiEvent("send event ${cnt++}", false))
        }
    }


    private data class UiEvent(
        val uiEvent: String,
        val jobNeed: Boolean
    )

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 保证[block]运行在主线程
     */
    private fun runInMainThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block.invoke()
        } else {
            mainHandler.post {
                block.invoke()
            }
        }
    }
}