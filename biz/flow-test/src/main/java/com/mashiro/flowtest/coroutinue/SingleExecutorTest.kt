package com.mashiro.flowtest.coroutinue

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

object SingletonExecutorCoroutine {
    val dispatchers = Executors.newSingleThreadExecutor {
        Thread(it).apply {
            name = "my-single-executor"
        }
    }.asCoroutineDispatcher()
}

object FixedExecutorCoroutine {
    val dispatchers = Executors.newFixedThreadPool(3) {
        Thread(it).apply {
            name = "my-fixed-executor"
        }
    }.asCoroutineDispatcher()
}

object SingleExecutorTest {

    private val a = AtomicInteger(0)
    private val b = AtomicInteger(0)
    fun Channel<Any>.go(block: suspend CoroutineScope.() -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d("test-coroutine", "before send " + a.getAndIncrement())
            send(0)
            Log.i("test-coroutine", "after send " + b.getAndIncrement())
            coroutineScope {
                block()
                receive()
            }
        }
    }

    val channel = Channel<Any>(2)

    fun testConcurrent() {
        // 并发限制为1，串行执行任务

        repeat(10) { x ->
            CoroutineScope(SingletonExecutorCoroutine.dispatchers + SupervisorJob()).launch {
                Log.w("test-coroutine", "send " + a.getAndIncrement())
                Log.w("test-coroutine", "$x going job")
                delay(1000L)
                Log.w("test-coroutine", "$x done job")
            }
        }
    }

    fun testBlocking() {
        CoroutineScope(SingletonExecutorCoroutine.dispatchers).launch {
            while (true) {
                val a = 0
            }
        }
    }

    fun testInject() {
        CoroutineScope(SingletonExecutorCoroutine.dispatchers).launch {
            Log.d("zyc", "test inject!!")
        }
    }

    fun testPriority() {
        CoroutineScope(FixedExecutorCoroutine.dispatchers).launch {
            Log.d("zyc", "test priority1!!")
//            delay(1000)
            Log.d("zyc", "test priority1Finish!!")
        }
        CoroutineScope(FixedExecutorCoroutine.dispatchers).launch {
            Log.d("zyc", "test priority2!!")
//            delay(1000)
            Log.d("zyc", "test priority2Finish!!")
        }
    }

    fun testPriority2() {
        val job1 = CoroutineScope(SingletonExecutorCoroutine.dispatchers).launch {
            Log.d("zyc", "test priority1!!")
            delay(1000)
            Log.d("zyc", "test priority1Finish!!")
        }
        CoroutineScope(SingletonExecutorCoroutine.dispatchers).launch {
            if (job1.isActive) {
                job1.join()
            }
            Log.d("zyc", "test priority2!!")
            delay(1000)
            Log.d("zyc", "test priority2Finish!!")
        }
    }

    fun testLaunchTime() {
        for (i in 0..10) {
            val startTime = System.currentTimeMillis()
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("zyc", "launch time $i ${System.currentTimeMillis() - startTime}")
                delay(1000)
            }
        }
    }

}