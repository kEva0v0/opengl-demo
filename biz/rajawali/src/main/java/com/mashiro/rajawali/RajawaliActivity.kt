package com.mashiro.rajawali

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_rajawali.*

class RajawaliActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_rajawali)
    }

    override fun onStart() {
        super.onStart()
        sv_full_view.cameraDistance
    }

}