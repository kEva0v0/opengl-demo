package com.mashiro.flowtest

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun AppCompatActivity.repeatOnLifecycleExt(
    state: Lifecycle.State,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(state, block)
    }
}

fun Fragment.repeatOnLifecycleExt(
    state: Lifecycle.State,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(state, block)
    }
}