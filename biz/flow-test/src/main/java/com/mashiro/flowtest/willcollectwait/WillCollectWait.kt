package com.mashiro.flowtest.willcollectwait

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class WillCollectWait {

    private val dispatchers = Executors.newSingleThreadExecutor{
        Thread(it).apply { this.name = "Will-Collect-wait" }
    }.asCoroutineDispatcher()

    private val coroutineScope = CoroutineScope(dispatchers)

    private val testFlow = MutableSharedFlow<Int>()

    fun sender() {
        var begin = 0
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(500)
                Log.d("zyc", "${this@WillCollectWait.javaClass.simpleName} sender $begin")
                testFlow.emit(begin++)
            }
        }
    }


    fun receiver() {
        coroutineScope.launch {
            testFlow.collect{
                Log.d("zyc", "${this@WillCollectWait.javaClass.simpleName} collect $it")
                delay(2000)
                Log.d("zyc", "${this@WillCollectWait.javaClass.simpleName} collect $it finished")
            }
        }
    }
}