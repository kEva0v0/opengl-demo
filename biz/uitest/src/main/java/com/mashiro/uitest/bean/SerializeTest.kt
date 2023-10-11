package com.mashiro.uitest.bean

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject

object SerializeTest {

    fun test() {
        val testJson = JSONObject().apply {
            this.put("test", JSONObject().apply {
                this.put("test_one", "1")
            })
            this.put("test_two", JSONObject().apply {
                this.put("test_one", "2")
            })
        }
        val str = testJson.toString()
        val tt = Gson().fromJson<TestSerialize>(str, TestSerialize::class.java)
        Log.d("zyc", "tt ${tt} and ${tt.testTwo.testOne}")
    }
}