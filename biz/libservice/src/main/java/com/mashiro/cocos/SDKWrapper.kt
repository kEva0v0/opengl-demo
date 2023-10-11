/****************************************************************************
 * Copyright (c) 2017-2020 Xiamen Yaji Software Co., Ltd.
 * http://www.cocos.com
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated engine source code (the "Software"), a limited,
 * worldwide, royalty-free, non-assignable, revocable and non-exclusive license
 * to use Cocos Creator solely to develop games on your target platforms. You shall
 * not use Cocos Creator software for developing other software or tools that's
 * used for developing games. You are not granted to publish, distribute,
 * sublicense, and/or sell copies of Cocos Creator.
 * The software or tools in this License Agreement are licensed, not sold.
 * Xiamen Yaji Software Co., Ltd. reserves all rights not expressly granted to you.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mashiro.cocos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference

class SDKWrapper private constructor() {
    private object SDKWrapperInstance {
        val mInstance = SDKWrapper()
    }

    interface SDKInterface {
        fun init(context: Context?) {}
        fun onStart() {}
        fun onPause() {}
        fun onResume() {}
        fun onStop() {}
        fun onDestroy() {}
        fun onRestart() {}
        fun onNewIntent(intent: Intent?) {}
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}
        fun onConfigurationChanged(newConfig: Configuration?) {}
        fun onRestoreInstanceState(savedInstanceState: Bundle?) {}
        fun onSaveInstanceState(outState: Bundle?) {}
        fun onBackPressed() {}
        fun onLowMemory() {}
    }

    private var mActivity: WeakReference<Activity>? = null
    private var serviceInstances: List<SDKInterface>? = null
    private fun loadSDKInterface() {
        val instances = ArrayList<SDKInterface>()
        try {
            val json = getJson("service.json")
            val jsonObject = JSONObject(json)
            val serviceClasses = jsonObject.getJSONArray("serviceClasses") ?: return
            val length = serviceClasses.length()
            for (i in 0 until length) {
                instances.add(
                    Class.forName(serviceClasses.getString(i)).newInstance() as SDKInterface
                )
            }
        } catch (ignored: Exception) {
        }
        serviceInstances = instances
    }

    private fun getJson(fileName: String): String {
        val sb = StringBuilder()
        try {
            val am = mActivity!!.get()!!.assets
            val br = BufferedReader(InputStreamReader(am.open(fileName)))
            var next: String?
            while (null != br.readLine().also { next = it }) {
                sb.append(next)
            }
        } catch (e: IOException) {
            sb.delete(0, sb.length)
        }
        return sb.toString().trim { it <= ' ' }
    }

    val activity: Activity?
        get() = mActivity!!.get()

    fun init(activity: Activity) {
        mActivity = WeakReference(activity)
        loadSDKInterface()
        for (sdk in serviceInstances!!) {
            sdk.init(activity)
        }
    }

    fun onResume() {
        for (sdk in serviceInstances!!) {
            sdk.onResume()
        }
    }

    fun onPause() {
        for (sdk in serviceInstances!!) {
            sdk.onPause()
        }
    }

    fun onDestroy() {
        for (sdk in serviceInstances!!) {
            sdk.onDestroy()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        for (sdk in serviceInstances!!) {
            sdk.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun onNewIntent(intent: Intent?) {
        for (sdk in serviceInstances!!) {
            sdk.onNewIntent(intent)
        }
    }

    fun onRestart() {
        for (sdk in serviceInstances!!) {
            sdk.onRestart()
        }
    }

    fun onStop() {
        for (sdk in serviceInstances!!) {
            sdk.onStop()
        }
    }

    fun onBackPressed() {
        for (sdk in serviceInstances!!) {
            sdk.onBackPressed()
        }
    }

    fun onConfigurationChanged(newConfig: Configuration?) {
        for (sdk in serviceInstances!!) {
            sdk.onConfigurationChanged(newConfig)
        }
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        for (sdk in serviceInstances!!) {
            sdk.onRestoreInstanceState(savedInstanceState)
        }
    }

    fun onSaveInstanceState(outState: Bundle?) {
        for (sdk in serviceInstances!!) {
            sdk.onSaveInstanceState(outState)
        }
    }

    fun onStart() {
        for (sdk in serviceInstances!!) {
            sdk.onStart()
        }
    }

    fun onLowMemory() {
        for (sdk in serviceInstances!!) {
            sdk.onLowMemory()
        }
    }

    companion object {
        fun shared(): SDKWrapper {
            return SDKWrapperInstance.mInstance
        }
    }
}