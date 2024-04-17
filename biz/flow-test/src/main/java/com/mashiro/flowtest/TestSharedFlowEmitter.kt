package com.mashiro.flowtest

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.logging.Logger

object TestSharedFlowEmitter {
    private val dispatcher = Executors.newSingleThreadExecutor {
        Thread(it).apply { name = "Test Shared flow" }
    }.asCoroutineDispatcher()

    private val coroutineScope = CoroutineScope(dispatcher)

    private val sharedFlowtest = MutableStateFlow<String>("start")

    private var emitTimer : Timer? = null
    private var emitCounting = 0

    fun getFlow(): Flow<String> = sharedFlowtest

    fun stopEmit() {
        emitTimer?.cancel()
        emitTimer = null
        emitCounting = 0
    }

    fun startEmit() {
        stopEmit()
        emitTimer = Timer()
        emitTimer?.schedule(object : TimerTask() {
            override fun run() {
                coroutineScope.launch {
                    val currentEmit = emitCounting++
                    Log.d("zyc", "emitting on $currentEmit")
                    sharedFlowtest.emit("emitting: ${currentEmit}")
                }
            }
        }, 0, 2000)
    }
}