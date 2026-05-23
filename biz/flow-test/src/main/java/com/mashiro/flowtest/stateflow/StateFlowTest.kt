package com.mashiro.flowtest.stateflow

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Created by zhangyichen on 5/26/25
 * @author zhangyichen.edden@bytedance.com
 */
data class TestData(
    val num: Int,
)

object StateFlowTest {

    private val testFlow = MutableStateFlow<TestData>(TestData(0))

    fun collect() {
        CoroutineScope(Dispatchers.IO).launch {
            testFlow.collect {
                delay(2000)
                Log.d("test", "collect $it")
            }
        }
    }

    fun emit() {
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 1..10) {
                delay(500)
                Log.d("test", "emit $i")
                testFlow.emit(TestData(i))
            }
        }
    }


    fun collect2() {
        CoroutineScope(Dispatchers.IO).launch {
            testFlow.collect {
                Log.d("zyc", "collect2 $it")
            }
        }
    }
    fun emit2() {
        CoroutineScope(Dispatchers.IO).launch {
            testFlow.emit(TestData(100))
            delay(1000)
            testFlow.emit(TestData(100))
        }
    }

    fun go() {
        collect()
        emit()
    }
}