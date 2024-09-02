package com.mashiro.flowtest

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.internal.ChannelFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class FlowViewModel: ViewModel() {

    private val currentChannel = Channel<String>(Channel.UNLIMITED)
    private var job: Job? = null

    fun collecting(activity: AppCompatActivity) {
        viewModelScope.launch {
            TestSharedFlowEmitter.getFlow()
                .collect{
                    Log.d("zyc", "channel send")
                    currentChannel.send(it)
                }
        }

    }

    fun startEmit() {
        TestSharedFlowEmitter.startEmit()
    }

    fun startCollect(activity: AppCompatActivity){
        job = activity.repeatOnLifecycleExt(Lifecycle.State.RESUMED){
            currentChannel.receiveAsFlow()
                .collect {
                    delay(5000)
                    Log.d("zyc", "collecting $it")
                }
        }
        job?.start()
    }

    fun testFlow(){
        val flow = flowOf(1,2,3)
        val flowInternal = flowOf(1,2,3)
        val coroutineScope = CoroutineScope(
            Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        ).launch {
            flow.collect{v1 ->
                pending(v1)
                flowInternal.collect{v2 ->
                    Log.d("zyc", "pending $v1, $v2")
                }
            }
        }
    }

    suspend fun pending(value: Int) {
        delay(1000)
        Log.d("zyc", "pending $value")
        delay(1000)
        Log.d("zyc", "pending end $value")
    }

    fun stopCollect() {
        job?.cancel()
    }

    fun stopEmit() {
        TestSharedFlowEmitter.stopEmit()
    }
}