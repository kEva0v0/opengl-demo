package com.mashiro.cocos

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import com.cocos.lib.CocosActivity

class CocosActivityDemo : CocosActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // DO OTHER INITIALIZATION BELOW
        SDKWrapper.shared().init(this)
    }

    override fun onResume() {
        super.onResume()
        SDKWrapper.shared().onResume()
    }

    override fun onPause() {
        super.onPause()
        SDKWrapper.shared().onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Workaround in https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508
        if (!isTaskRoot) {
            return
        }
        SDKWrapper.shared().onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        SDKWrapper.shared().onActivityResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        SDKWrapper.shared().onNewIntent(intent)
    }

    override fun onRestart() {
        super.onRestart()
        SDKWrapper.shared().onRestart()
    }

    override fun onStop() {
        super.onStop()
        SDKWrapper.shared().onStop()
    }

    override fun onBackPressed() {
        SDKWrapper.shared().onBackPressed()
        super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        SDKWrapper.shared().onConfigurationChanged(newConfig)
        super.onConfigurationChanged(newConfig)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        SDKWrapper.shared().onRestoreInstanceState(savedInstanceState)
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        SDKWrapper.shared().onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        SDKWrapper.shared().onStart()
        super.onStart()
    }

    override fun onLowMemory() {
        SDKWrapper.shared().onLowMemory()
        super.onLowMemory()
    }
}